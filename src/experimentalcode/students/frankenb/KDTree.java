package experimentalcode.students.frankenb;

import java.util.HashSet;
import java.util.Set;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.ids.ArrayModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBIDFactory;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.PrimitiveDoubleDistanceFunction;
import de.lmu.ifi.dbs.elki.distance.distanceresultlist.DoubleDistanceKNNHeap;
import de.lmu.ifi.dbs.elki.distance.distanceresultlist.KNNResult;
import de.lmu.ifi.dbs.elki.distance.distancevalue.DoubleDistance;
import de.lmu.ifi.dbs.elki.utilities.DatabaseUtil;

/**
 * No description given.
 * 
 * @author Florian Frankenberger
 */
public class KDTree<V extends NumberVector<?>> {
  private final Relation<V> relation;

  private final KDTreeNode root;

  public KDTree(Relation<V> dataSet) {
    this.relation = dataSet;
    this.root = buildTree(null, dataSet, dataSet.getDBIDs(), 0);
  }

  private KDTreeNode buildTree(KDTreeNode parent, Relation<V> dataSet, DBIDs ids, int depth) {
    if(ids.size() == 0) {
      return null;
    }

    final int dimension = (depth % DatabaseUtil.dimensionality(dataSet)) + 1;
    final double median = DatabaseUtil.exactMedian(dataSet, ids, dimension);

    KDTreeNode newNode = new KDTreeNode(parent, dimension, median);

    ArrayModifiableDBIDs[] splitIDs = splitAtMedian(dataSet, ids, dimension, median);
    newNode.leftChild = buildTree(newNode, dataSet, splitIDs[0], depth + 1);
    newNode.ids = splitIDs[1];
    newNode.rightChild = buildTree(newNode, dataSet, splitIDs[2], depth + 1);

    return newNode;
  }

  /**
   * Finds the k neighborhood.
   * 
   * @param id
   * @param k
   * @param distanceFunction
   * @return
   */
  public KNNResult<DoubleDistance> findNearestNeighbors(DBIDRef id, int k, PrimitiveDoubleDistanceFunction<V> distanceFunction) {
    V vector = this.relation.get(id);
    KDTreeNode node = searchNodeFor(vector, this.root);

    DoubleDistanceKNNHeap distanceList = new DoubleDistanceKNNHeap(k);
    Set<KDTreeNode> alreadyVisited = new HashSet<KDTreeNode>();

    findNeighbors(k, distanceFunction, vector, node, distanceList, alreadyVisited);
    return distanceList.toKNNList();
  }

  private void findNeighbors(int k, PrimitiveDoubleDistanceFunction<V> distanceFunction, V queryVector, KDTreeNode currentNode, DoubleDistanceKNNHeap distanceList, Set<KDTreeNode> alreadyVisited) {
    double maxdist = distanceList.doubleKNNDistance();
    for(DBIDIter id = currentNode.ids.iter(); id.valid(); id.advance()) {
      double distanceToId = distanceFunction.doubleDistance(queryVector, relation.get(id));
      if (distanceToId < maxdist) {
        distanceList.add(DBIDFactory.FACTORY.newDistancePair(distanceToId, id));
        maxdist = distanceList.doubleKNNDistance();
      }
    }

    alreadyVisited.add(currentNode);
    if(!currentNode.isLeaf()) {
      double splitDistance = Math.abs(currentNode.splitPoint - queryVector.doubleValue(currentNode.dimension));

      if(splitDistance <= maxdist) {
        if(currentNode.leftChild != null && !alreadyVisited.contains(currentNode.leftChild)) {
          findNeighbors(k, distanceFunction, queryVector, currentNode.leftChild, distanceList, alreadyVisited);
        }
      }
      if(splitDistance <= maxdist) {
        if(currentNode.rightChild != null && !alreadyVisited.contains(currentNode.rightChild)) {
          findNeighbors(k, distanceFunction, queryVector, currentNode.rightChild, distanceList, alreadyVisited);
        }
      }
    }

    if(currentNode != root && !alreadyVisited.contains(currentNode.parent)) {
      findNeighbors(k, distanceFunction, queryVector, currentNode.parent, distanceList, alreadyVisited);
    }
  }

  private KDTreeNode searchNodeFor(V vector, KDTreeNode node) {
    if(!node.isLeaf()) {
      double value = vector.doubleValue(node.dimension);

      if(value < node.splitPoint && node.leftChild != null) {
        return searchNodeFor(vector, node.leftChild);
      }

      if(value >= node.splitPoint && node.rightChild != null) {
        return searchNodeFor(vector, node.rightChild);
      }
    }
    return node;
  }

  /**
   * Splits a data set according to the dimension and position into two data
   * sets. If dimension is 1 the data gets split into two data sets where all
   * points with x < position get into one data set and all points >= position
   * get into the other
   * 
   * @param relation Relation to split
   * @param ids DBIDs to process
   * @param dimension Dimension to use
   * @param position Split position
   */
  public static <V extends NumberVector<?>> ArrayModifiableDBIDs[] splitAtMedian(Relation<V> relation, DBIDs ids, int dimension, double position) {
    ArrayModifiableDBIDs dataSetLower = DBIDUtil.newArray((int) (ids.size() * 0.51));
    ArrayModifiableDBIDs dataSetExact = DBIDUtil.newArray((int) (ids.size() * 0.05));
    ArrayModifiableDBIDs dataSetHigher = DBIDUtil.newArray((int) (ids.size() * 0.51));
    for(DBIDIter id = ids.iter(); id.valid(); id.advance()) {
      double val = relation.get(id).doubleValue(dimension);
      if(val < position) {
        dataSetLower.add(id);
      }
      else if(val > position) {
        dataSetHigher.add(id);
      }
      else {
        dataSetExact.add(id);
      }
    }
    return new ArrayModifiableDBIDs[] { dataSetLower, dataSetExact, dataSetHigher };
  }

  private static class KDTreeNode {
  
    final int dimension;
  
    final KDTreeNode parent;
  
    final double splitPoint;
  
    ArrayModifiableDBIDs ids = DBIDUtil.newArray();
  
    KDTreeNode leftChild, rightChild;
  
    KDTreeNode(KDTreeNode parent, int dimension, double splitPoint) {
      this.parent = parent;
      this.dimension = dimension;
      this.splitPoint = splitPoint;
    }
  
    boolean isLeaf() {
      return this.leftChild == null && this.rightChild == null;
    }
  }
}