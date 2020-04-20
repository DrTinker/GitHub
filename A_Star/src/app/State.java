package app;

public class State {
    Value[][] matrix; //可变长二维数组
    int x_pos;
    int y_pos;
}

class Value{
    int Manhattan_Distance;
    int signal;
}