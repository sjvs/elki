/*
 * This file is part of ELKI:
 * Environment for Developing KDD-Applications Supported by Index-Structures
 *
 * Copyright (C) 2017
 * ELKI Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lmu.ifi.dbs.elki.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.utilities.datastructures.hierarchy.Hierarchy;

/**
 * Utilities for handling result objects
 *
 * @author Erich Schubert
 * @since 0.2
 *
 * @apiviz.uses Result oneway - - filters
 */
public class ResultUtil {
  /**
   * Collect all Annotation results from a Result
   *
   * @param r Result
   * @return List of all annotation results
   */
  public static List<Relation<?>> getRelations(Result r) {
    if(r instanceof Relation<?>) {
      List<Relation<?>> anns = new ArrayList<>(1);
      anns.add((Relation<?>) r);
      return anns;
    }
    if(r instanceof HierarchicalResult) {
      return filterResults(((HierarchicalResult) r).getHierarchy(), r, Relation.class);
    }
    return Collections.emptyList();
  }

  /**
   * Collect all ordering results from a Result
   *
   * @param r Result
   * @return List of ordering results
   */
  public static List<OrderingResult> getOrderingResults(Result r) {
    if(r instanceof OrderingResult) {
      List<OrderingResult> ors = new ArrayList<>(1);
      ors.add((OrderingResult) r);
      return ors;
    }
    if(r instanceof HierarchicalResult) {
      return filterResults(((HierarchicalResult) r).getHierarchy(), r, OrderingResult.class);
    }
    return Collections.emptyList();
  }

  /**
   * Collect all collection results from a Result
   *
   * @param r Result
   * @return List of collection results
   */
  public static List<CollectionResult<?>> getCollectionResults(Result r) {
    if(r instanceof CollectionResult<?>) {
      List<CollectionResult<?>> crs = new ArrayList<>(1);
      crs.add((CollectionResult<?>) r);
      return crs;
    }
    if(r instanceof HierarchicalResult) {
      return filterResults(((HierarchicalResult) r).getHierarchy(), r, CollectionResult.class);
    }
    return Collections.emptyList();
  }

  /**
   * Return all Iterable results
   *
   * @param r Result
   * @return List of iterable results
   */
  public static List<IterableResult<?>> getIterableResults(Result r) {
    if(r instanceof IterableResult<?>) {
      List<IterableResult<?>> irs = new ArrayList<>(1);
      irs.add((IterableResult<?>) r);
      return irs;
    }
    if(r instanceof HierarchicalResult) {
      return filterResults(((HierarchicalResult) r).getHierarchy(), r, IterableResult.class);
    }
    return Collections.emptyList();
  }

  /**
   * Return only results of the given restriction class
   *
   * @param <C> Class type
   * @param hier Result hierarchy
   * @param r Starting position
   * @param restrictionClass Class restriction
   * @return filtered results list
   */
  // We can't ensure that restrictionClass matches C exactly.
  @SuppressWarnings("unchecked")
  public static <C extends Result> ArrayList<C> filterResults(ResultHierarchy hier, Result r, Class<? super C> restrictionClass) {
    ArrayList<C> res = new ArrayList<>();
    if(restrictionClass.isInstance(r)) {
      res.add((C) restrictionClass.cast(r));
    }
    for(Hierarchy.Iter<Result> iter = hier.iterDescendants(r); iter.valid(); iter.advance()) {
      Result result = iter.get();
      if(restrictionClass.isInstance(result)) {
        res.add((C) restrictionClass.cast(result));
      }
    }
    return res;
  }

  /**
   * Return only results of the given restriction class
   *
   * @param <C> Class type
   * @param hier Result hierarchy
   * @param restrictionClass Class restriction
   * @return filtered results list
   */
  // We can't ensure that restrictionClass matches C exactly.
  @SuppressWarnings("unchecked")
  public static <C extends Result> ArrayList<C> filterResults(ResultHierarchy hier, Class<? super C> restrictionClass) {
    ArrayList<C> res = new ArrayList<>();
    for(Hierarchy.Iter<Result> iter = hier.iterAll(); iter.valid(); iter.advance()) {
      Result result = iter.get();
      if(restrictionClass.isInstance(result)) {
        res.add((C) restrictionClass.cast(result));
      }
    }
    return res;
  }

  /**
   * Add a child result.
   *
   * @param parent Parent
   * @param child Child
   */
  public static void addChildResult(HierarchicalResult parent, Result child) {
    parent.getHierarchy().add(parent, child);
  }

  /**
   * Find the first database result in the tree.
   *
   * @param baseResult Result tree base.
   * @return Database
   */
  public static Database findDatabase(ResultHierarchy hier, Result baseResult) {
    final List<Database> dbs = filterResults(hier, baseResult, Database.class);
    return (!dbs.isEmpty()) ? dbs.get(0) : null;
  }

  /**
   * Find the first database result in the tree.
   *
   * @param hier Result hierarchy.
   * @return Database
   */
  public static Database findDatabase(ResultHierarchy hier) {
    final List<Database> dbs = filterResults(hier, Database.class);
    return (!dbs.isEmpty()) ? dbs.get(0) : null;
  }

  /**
   * Recursively remove a result and its children.
   *
   * @param hierarchy Result hierarchy
   * @param child Result to remove
   */
  public static void removeRecursive(ResultHierarchy hierarchy, Result child) {
    for(Hierarchy.Iter<Result> iter = hierarchy.iterParents(child); iter.valid(); iter.advance()) {
      hierarchy.remove(iter.get(), child);
    }
    for(Hierarchy.Iter<Result> iter = hierarchy.iterChildren(child); iter.valid(); iter.advance()) {
      removeRecursive(hierarchy, iter.get());
    }
  }
}
