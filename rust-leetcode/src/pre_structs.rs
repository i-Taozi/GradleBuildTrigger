use std::cell::RefCell;
use std::fmt::{Debug, Display};
use std::rc::Rc;

pub(crate) struct Solution;

//二叉树
#[derive(Debug, PartialEq, Eq)]
pub struct TreeNode {
    pub val: i32,
    pub left: Option<Rc<RefCell<TreeNode>>>,
    pub right: Option<Rc<RefCell<TreeNode>>>,
}

impl TreeNode {
    #[inline]
    pub fn new(val: i32) -> Self {
        TreeNode {
            val,
            left: None,
            right: None,
        }
    }
}

//单链表
#[derive(PartialEq, Eq, Clone, Debug)]
pub struct ListNode {
    pub val: i32,
    pub next: Option<Box<ListNode>>, //堆上
}

impl ListNode {
    #[inline]
    fn new(val: i32) -> Self {
        ListNode { next: None, val }
    }
}

pub fn get_test_list_1() -> Option<Box<ListNode>> {
    let node2 = Some(Box::new(ListNode::new(9)));
    let node1 = Some(Box::new(ListNode {
        val: 3,
        next: node2,
    }));
    node1
}

pub fn get_test_tree_5() -> Option<Rc<RefCell<TreeNode>>> {
    let e7 = Some(Rc::new(RefCell::new(TreeNode {
        val: 7,
        left: None,
        right: None,
    })));
    let e15 = Some(Rc::new(RefCell::new(TreeNode {
        val: 15,
        left: None,
        right: None,
    })));
    let e20 = Some(Rc::new(RefCell::new(TreeNode {
        val: 20,
        left: e15,
        right: e7,
    })));
    let e9 = Some(Rc::new(RefCell::new(TreeNode {
        val: 9,
        left: None,
        right: None,
    })));
    let e3 = Some(Rc::new(RefCell::new(TreeNode {
        val: 3,
        left: e9,
        right: e20,
    })));

    e3
}

pub fn get_test_tree_3() -> Option<Rc<RefCell<TreeNode>>> {
    let e2 = Some(Rc::new(RefCell::new(TreeNode {
        val: 2,
        left: None,
        right: None,
    })));
    let e1 = Some(Rc::new(RefCell::new(TreeNode {
        val: 2,
        left: None,
        right: None,
    })));
    let root1 = Some(Rc::new(RefCell::new(TreeNode {
        val: 1,
        left: e1,
        right: e2,
    })));

    root1
}

pub fn get_test_tree_2() -> Option<Rc<RefCell<TreeNode>>> {
    let e1 = Some(Rc::new(RefCell::new(TreeNode {
        val: 2,
        left: None,
        right: None,
    })));
    let root1 = Some(Rc::new(RefCell::new(TreeNode {
        val: 1,
        left: None,
        right: e1,
    })));

    root1
}

pub fn get_test_tree_4() -> Option<Rc<RefCell<TreeNode>>> {
    let e3 = Some(Rc::new(RefCell::new(TreeNode {
        val: 2,
        left: None,
        right: None,
    })));
    let e2 = Some(Rc::new(RefCell::new(TreeNode {
        val: 2,
        left: None,
        right: e3,
    })));
    let e1 = Some(Rc::new(RefCell::new(TreeNode {
        val: 2,
        left: None,
        right: e2,
    })));
    let root1 = Some(Rc::new(RefCell::new(TreeNode {
        val: 1,
        left: None,
        right: e1,
    })));

    root1
}

pub fn print_vec<T: Display>(nums: Vec<T>) {
    for e in nums.iter() {
        println!("{}", e);
    }
}

pub fn print_vec_without_enter<T: Display>(nums: Vec<T>) {
    for e in nums.iter() {
        print!("{}", e);
    }
}
