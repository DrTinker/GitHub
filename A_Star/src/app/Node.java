package app;

/**
 * Node
 * 1.current state
 * 2.parent node
 * 3.action(how parent get to the current state)
 * 4.path cost
 */
public class Node {
    State cur_state;
    Node pre_node;
    int acton;
    int path_cost;
    
    Node(){}
}