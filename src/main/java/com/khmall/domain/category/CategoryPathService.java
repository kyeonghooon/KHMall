package com.khmall.domain.category;

import com.khmall.domain.category.CategoryRepository.Flat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryPathService {

  private final CategoryRepository categoryRepository;

  /**
   * 요청된 카테고리 ID의 경로를 반환합니다.
   *
   * @param categoryId 요청된 카테고리 ID
   * @return 카테고리 경로 문자열
   */
  public String buildPath(Long categoryId) {
    if (categoryId == null) return null;
    List<Flat> flats = categoryRepository.findAllFlat();
    Map<Long, Flat> all = flats.stream()
        .collect(Collectors.toMap(Flat::getId, f -> f));
    return pathOf(categoryId, all, new HashMap<>());
  }

  /**
   * 요청된 카테고리 ID들의 경로를 반환합니다.
   *
   * @param categoryIds 요청된 카테고리 ID들의 집합
   * @return 카테고리 ID와 해당 경로의 맵
   */
  public Map<Long, String> buildPathsFor(Set<Long> categoryIds) {
    if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyMap();

    List<Flat> flats = categoryRepository.findAllFlat();
    Map<Long, Flat> all = flats.stream()
        .collect(Collectors.toMap(Flat::getId, f -> f));

    Map<Long, String> memo = new HashMap<>();
    for (Long id : categoryIds) pathOf(id, all, memo);

    // 요청된 것만 반환
    Map<Long, String> result = new HashMap<>();
    for (Long id : categoryIds) result.put(id, memo.get(id));
    return result;
  }

  /**
   * 카테고리 ID로부터 경로를 재귀적으로 찾습니다.
   *
   * @param id   카테고리 ID
   * @param all  모든 카테고리 정보를 담은 맵
   * @param memo 이미 계산된 경로를 저장하는 맵
   * @return 카테고리 경로 문자열
   */
  private String pathOf(Long id, Map<Long, Flat> all, Map<Long, String> memo) {
    if (id == null) return null;
    if (memo.containsKey(id)) return memo.get(id);

    Flat cur = all.get(id);
    if (cur == null) {
      memo.put(id, null);
      return null;
    }

    String parentPath = pathOf(cur.getParentId(), all, memo);
    String path = (parentPath == null) ? cur.getName() : parentPath + " > " + cur.getName();
    memo.put(id, path);
    return path;
  }

  /**
   * 주어진 카테고리 ID의 모든 하위 카테고리 ID를 수집합니다.
   *
   * @param rootCategoryId 루트 카테고리 ID
   * @return 하위 카테고리 ID들의 집합
   */
  public Set<Long> collectDescendantIds(Long rootCategoryId) {
    if (rootCategoryId == null) return Collections.emptySet();

    List<CategoryRepository.Flat> flats = categoryRepository.findAllFlat();

    // parentId -> childrenIds 매핑
    Map<Long, List<Long>> children = new HashMap<>();
    for (CategoryRepository.Flat f : flats) {
      Long pid = f.getParentId();
      if (pid != null) {
        children.computeIfAbsent(pid, k -> new ArrayList<>()).add(f.getId());
      }
    }

    Set<Long> result = new LinkedHashSet<>();
    Deque<Long> stack = new ArrayDeque<>();
    stack.push(rootCategoryId);
    result.add(rootCategoryId);

    while (!stack.isEmpty()) {
      Long cur = stack.pop();
      List<Long> kids = children.get(cur);
      if (kids == null) continue;
      for (Long kid : kids) {
        if (result.add(kid)) stack.push(kid);
      }
    }
    return result;
  }
}
