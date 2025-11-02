package nxcloud.foundation.core.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeHelperJavaTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testBuildTree() {
        List<TreeNode> tree = TreeHelper.buildTree(
                treeData(),
                TreeNode::getId,
                TreeNode::getParentId,
                pid -> pid.isEmpty(),
                (node, children) -> {
                    node.setChildren((List<TreeNode>) children);
                    return null;
                },
                Comparator.comparing(TreeNode::getSortValue)
        );

        System.out.println(tree);
    }

    private List<TreeNode> treeData() {
        List<TreeNode> data = new ArrayList<>();

        data.add(new TreeNode("1", "Node 1", "", 0));
        data.add(new TreeNode("2", "Node 2", "1", 3));
        data.add(new TreeNode("3", "Node 3", "1", -1));
        data.add(new TreeNode("4", "Node 4", "", 5));
        data.add(new TreeNode("5", "Node 5", "-1", 3));
        data.add(new TreeNode("6", "Node 6", "5", 4));

        System.out.println(data);
        return data;
    }

    static public class TreeNode {
        private String id;

        private String name;

        private String parentId;

        private Integer sortValue;

        private List<TreeNode> children;

        TreeNode(String id, String name, String parentId, Integer sortValue) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;
            this.sortValue = sortValue;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public Integer getSortValue() {
            return sortValue;
        }

        public void setSortValue(Integer sortValue) {
            this.sortValue = sortValue;
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public void setChildren(List<TreeNode> children) {
            this.children = children;
        }
    }
}
