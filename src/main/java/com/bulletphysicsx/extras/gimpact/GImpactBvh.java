/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * This source file is part of GIMPACT Library.
 *
 * For the latest info, see http://gimpact.sourceforge.net/
 *
 * Copyright (c) 2007 Francisco Leon Najera. C.C. 80087371.
 * email: projectileman@yahoo.com
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysicsx.extras.gimpact;

import javax.vecmath.Vector3f;

import com.bulletphysicsx.extras.gimpact.BoxCollision.AABB;
import com.bulletphysicsx.extras.gimpact.BoxCollision.BoxBoxTransformCache;
import com.bulletphysicsx.linearmath.Transform;
import com.bulletphysicsx.util.IntArrayList;

/**
 * @author jezek2
 */
class GImpactBvh {

    protected BvhTree box_tree = new BvhTree();
    protected PrimitiveManagerBase primitive_manager;

    /**
     * This constructor doesn't build the tree. you must call buildSet.
     */
    public GImpactBvh() {
        primitive_manager = null;
    }

    /**
     * This constructor doesn't build the tree. you must call buildSet.
     */
    public GImpactBvh(PrimitiveManagerBase primitive_manager) {
        this.primitive_manager = primitive_manager;
    }

    public AABB getGlobalBox(AABB out) {
        getNodeBound(0, out);
        return out;
    }

    public void setPrimitiveManager(PrimitiveManagerBase primitive_manager) {
        this.primitive_manager = primitive_manager;
    }

    public PrimitiveManagerBase getPrimitiveManager() {
        return primitive_manager;
    }

    // stackless refit
    protected void refit() {
        AABB leafbox = new AABB();
        AABB bound = new AABB();
        AABB temp_box = new AABB();

        int nodecount = getNodeCount();
        while ((nodecount--) != 0) {
            if (isLeafNode(nodecount)) {
                primitive_manager.get_primitive_box(getNodeData(nodecount), leafbox);
                setNodeBound(nodecount, leafbox);
            } else {
                //const BT_BVH_TREE_NODE * nodepointer = get_node_pointer(nodecount);
                //get left bound
                bound.invalidate();

                int child_node = getLeftNode(nodecount);
                if (child_node != 0) {
                    getNodeBound(child_node, temp_box);
                    bound.merge(temp_box);
                }

                child_node = getRightNode(nodecount);
                if (child_node != 0) {
                    getNodeBound(child_node, temp_box);
                    bound.merge(temp_box);
                }

                setNodeBound(nodecount, bound);
            }
        }
    }

    /**
     * This attemps to refit the box set.
     */
    public void update() {
        refit();
    }

    /**
     * This rebuild the entire set.
     */
    public void buildSet() {
        // obtain primitive boxes
        BvhDataArray primitive_boxes = new BvhDataArray();
        primitive_boxes.resize(primitive_manager.get_primitive_count());

        AABB tmpAABB = new AABB();

        for (int i = 0; i < primitive_boxes.size(); i++) {
            //primitive_manager.get_primitive_box(i,primitive_boxes[i].bound);
            primitive_manager.get_primitive_box(i, tmpAABB);
            primitive_boxes.setBound(i, tmpAABB);

            primitive_boxes.setData(i, i);
        }

        box_tree.build_tree(primitive_boxes);
    }

    /**
     * Returns the indices of the primitives in the primitive_manager field.
     */
    public boolean boxQuery(AABB box, IntArrayList collided_results) {
        int curIndex = 0;
        int numNodes = getNodeCount();

        AABB bound = new AABB();

        while (curIndex < numNodes) {
            getNodeBound(curIndex, bound);

            // catch bugs in tree data

            boolean aabbOverlap = bound.has_collision(box);
            boolean isleafnode = isLeafNode(curIndex);

            if (isleafnode && aabbOverlap) {
                collided_results.add(getNodeData(curIndex));
            }

            if (aabbOverlap || isleafnode) {
                // next subnode
                curIndex++;
            } else {
                // skip node
                curIndex += getEscapeNodeIndex(curIndex);
            }
        }
        if (collided_results.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the indices of the primitives in the primitive_manager field.
     */
    public boolean boxQueryTrans(AABB box, Transform transform, IntArrayList collided_results) {
        AABB transbox = new AABB(box);
        transbox.appy_transform(transform);
        return boxQuery(transbox, collided_results);
    }

    /**
     * Returns the indices of the primitives in the primitive_manager field.
     */
    public boolean rayQuery(Vector3f ray_dir, Vector3f ray_origin, IntArrayList collided_results) {
        int curIndex = 0;
        int numNodes = getNodeCount();

        AABB bound = new AABB();

        while (curIndex < numNodes) {
            getNodeBound(curIndex, bound);

            // catch bugs in tree data

            boolean aabbOverlap = bound.collide_ray(ray_origin, ray_dir);
            boolean isleafnode = isLeafNode(curIndex);

            if (isleafnode && aabbOverlap) {
                collided_results.add(getNodeData(curIndex));
            }

            if (aabbOverlap || isleafnode) {
                // next subnode
                curIndex++;
            } else {
                // skip node
                curIndex += getEscapeNodeIndex(curIndex);
            }
        }
        if (collided_results.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * Tells if this set has hierarchy.
     */
    public boolean hasHierarchy() {
        return true;
    }

    /**
     * Tells if this set is a trimesh.
     */
    public boolean isTrimesh() {
        return primitive_manager.is_trimesh();
    }

    public int getNodeCount() {
        return box_tree.getNodeCount();
    }

    /**
     * Tells if the node is a leaf.
     */
    public boolean isLeafNode(int nodeindex) {
        return box_tree.isLeafNode(nodeindex);
    }

    public int getNodeData(int nodeindex) {
        return box_tree.getNodeData(nodeindex);
    }

    public void getNodeBound(int nodeindex, AABB bound) {
        box_tree.getNodeBound(nodeindex, bound);
    }

    public void setNodeBound(int nodeindex, AABB bound) {
        box_tree.setNodeBound(nodeindex, bound);
    }

    public int getLeftNode(int nodeindex) {
        return box_tree.getLeftNode(nodeindex);
    }

    public int getRightNode(int nodeindex) {
        return box_tree.getRightNode(nodeindex);
    }

    public int getEscapeNodeIndex(int nodeindex) {
        return box_tree.getEscapeNodeIndex(nodeindex);
    }

    public void getNodeTriangle(int nodeindex, PrimitiveTriangle triangle) {
        primitive_manager.get_primitive_triangle(getNodeData(nodeindex), triangle);
    }

    public BvhTreeNodeArray get_node_pointer() {
        return box_tree.get_node_pointer();
    }

    private static boolean _node_collision(GImpactBvh boxset0, GImpactBvh boxset1, BoxBoxTransformCache trans_cache_1to0, int node0, int node1, boolean complete_primitive_tests) {
        AABB box0 = new AABB();
        boxset0.getNodeBound(node0, box0);
        AABB box1 = new AABB();
        boxset1.getNodeBound(node1, box1);

        return box0.overlapping_trans_cache(box1, trans_cache_1to0, complete_primitive_tests);
        //box1.appy_transform_trans_cache(trans_cache_1to0);
        //return box0.has_collision(box1);
    }

    /**
     * Stackless recursive collision routine.
     */
    private static void _find_collision_pairs_recursive(GImpactBvh boxset0, GImpactBvh boxset1, PairSet collision_pairs, BoxBoxTransformCache trans_cache_1to0, int node0, int node1, boolean complete_primitive_tests) {
        if (_node_collision(
                boxset0, boxset1, trans_cache_1to0,
                node0, node1, complete_primitive_tests) == false) {
            return;//avoid colliding internal nodes
        }
        if (boxset0.isLeafNode(node0)) {
            if (boxset1.isLeafNode(node1)) {
                // collision result
                collision_pairs.push_pair(boxset0.getNodeData(node0), boxset1.getNodeData(node1));
                return;
            } else {
                // collide left recursive
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        node0, boxset1.getLeftNode(node1), false);

                // collide right recursive
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        node0, boxset1.getRightNode(node1), false);
            }
        } else {
            if (boxset1.isLeafNode(node1)) {
                // collide left recursive
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        boxset0.getLeftNode(node0), node1, false);


                // collide right recursive
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        boxset0.getRightNode(node0), node1, false);
            } else {
                // collide left0 left1
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        boxset0.getLeftNode(node0), boxset1.getLeftNode(node1), false);

                // collide left0 right1
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        boxset0.getLeftNode(node0), boxset1.getRightNode(node1), false);

                // collide right0 left1
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        boxset0.getRightNode(node0), boxset1.getLeftNode(node1), false);

                // collide right0 right1
                _find_collision_pairs_recursive(
                        boxset0, boxset1,
                        collision_pairs, trans_cache_1to0,
                        boxset0.getRightNode(node0), boxset1.getRightNode(node1), false);

            } // else if node1 is not a leaf
        } // else if node0 is not a leaf
    }

    //public static float getAverageTreeCollisionTime();

    public static void find_collision(GImpactBvh boxset0, Transform trans0, GImpactBvh boxset1, Transform trans1, PairSet collision_pairs) {
        if (boxset0.getNodeCount() == 0 || boxset1.getNodeCount() == 0) {
            return;
        }
        BoxBoxTransformCache trans_cache_1to0 = new BoxBoxTransformCache();

        trans_cache_1to0.calc_from_homogenic(trans0, trans1);

        //#ifdef TRI_COLLISION_PROFILING
        //bt_begin_gim02_tree_time();
        //#endif //TRI_COLLISION_PROFILING

        _find_collision_pairs_recursive(
                boxset0, boxset1,
                collision_pairs, trans_cache_1to0, 0, 0, true);

        //#ifdef TRI_COLLISION_PROFILING
        //bt_end_gim02_tree_time();
        //#endif //TRI_COLLISION_PROFILING
    }

}
