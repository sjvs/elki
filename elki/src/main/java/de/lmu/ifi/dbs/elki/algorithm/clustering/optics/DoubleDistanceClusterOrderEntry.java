package de.lmu.ifi.dbs.elki.algorithm.clustering.optics;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2014
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.result.textwriter.TextWriteable;
import de.lmu.ifi.dbs.elki.result.textwriter.TextWriterStream;

/**
 * Provides an entry in a cluster order.
 * 
 * @author Elke Achtert
 */
public class DoubleDistanceClusterOrderEntry implements ClusterOrderEntry<DoubleDistanceClusterOrderEntry>, TextWriteable {
  /**
   * The id of the entry.
   */
  private DBID objectID;

  /**
   * The id of the entry's predecessor.
   */
  private DBID predecessorID;

  /**
   * The reachability of the entry.
   */
  private double reachability;

  /**
   * Creates a new entry in a cluster order with the specified parameters.
   * 
   * @param objectID the id of the entry
   * @param predecessorID the id of the entry's predecessor
   * @param reachability the reachability of the entry
   */
  public DoubleDistanceClusterOrderEntry(DBID objectID, DBID predecessorID, double reachability) {
    this.objectID = objectID;
    this.predecessorID = predecessorID;
    this.reachability = reachability;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * 
   * NOTE: for the use in an UpdatableHeap, only the ID is compared!
   * 
   * @param o the reference object with which to compare.
   * @return <code>true</code> if this object has the same attribute values as
   *         the o argument; <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if(this == o) {
      return true;
    }
    if(!(o instanceof ClusterOrderEntry)) {
      return false;
    }

    final ClusterOrderEntry<?> that = (ClusterOrderEntry<?>) o;
    // Compare by ID only, for UpdatableHeap!
    return DBIDUtil.equal(objectID, that.getID());
  }

  /**
   * Returns a hash code value for the object.
   * 
   * @return the object id if this entry
   */
  @Override
  public int hashCode() {
    return objectID.hashCode();
  }

  /**
   * Returns a string representation of the object.
   * 
   * @return a string representation of the object.
   */
  @Override
  public String toString() {
    return objectID + "(" + predecessorID + "," + reachability + ")";
  }

  /**
   * Returns the object id of this entry.
   * 
   * @return the object id of this entry
   */
  @Override
  public DBID getID() {
    return objectID;
  }

  /**
   * Returns the id of the predecessor of this entry if this entry has a
   * predecessor, null otherwise.
   * 
   * @return the id of the predecessor of this entry
   */
  @Override
  public DBID getPredecessorID() {
    return predecessorID;
  }

  /**
   * Returns the reachability distance of this entry
   * 
   * @return the reachability distance of this entry
   */
  public double getReachability() {
    return reachability;
  }

  @Override
  public int compareTo(DoubleDistanceClusterOrderEntry o) {
    if(this.reachability < o.reachability) {
      return -1;
    }
    if(this.reachability > o.reachability) {
      return +1;
    }
    return -getID().compareTo(o.getID());
  }

  @Override
  public void writeToText(TextWriterStream out, String label) {
    out.inlinePrint("predecessor=" + DBIDUtil.toString((DBIDRef) predecessorID));
    out.inlinePrint("reachability=" + reachability);
  }
}