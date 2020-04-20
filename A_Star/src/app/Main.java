package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static int BARRIER = -1;
    private static int SPACE = 1;
    private static int PASSED = 2;
    private static int START = 3;
    private static int END = 4;
    private static int PATH = 5;
    private static int WALL = 6;

    public static void main(String[] args) throws IOException {
        //frontier 
        //Astar算法使用曼哈顿距离和cost来选择下一个状态，因此这里简单地用Arraylist来存储就行
        ArrayList<Node> frontier = new ArrayList<>();
        //Stack<Node> frontier_DFS = new Stack<>();     DFS用栈
        //Queue<Node> frontier_BFS = new LinkedList<>();    BFS用队列

        //记录已经探索过的节点,这里用Set是因为同一个节点只保存一次
        Set<Node> exploered = new HashSet<>();

        System.out.println("请输入迷宫文件:");
        Scanner scanner = new Scanner(System.in);
        String FileName = scanner.next();
        scanner.close();

        //计算解决问题状态数
        int count = 0;
        //定义初始状态
        State init_state = new State();
        init_state = input(FileName, START);
        System.out.println("迷宫图如下：");
        output(init_state.matrix, BARRIER);
        System.out.println("\n");
        //确定终点坐标
        State final_state = new State();
        final_state = input(FileName, END);
        //计算初始状态每隔的曼哈顿距离
        manhattanSet(init_state, final_state.x_pos, final_state.y_pos);
        
        //设置初始node
        Node init_node = new Node();
        init_node.cur_state = init_state; //初始状态
        init_node.pre_node = null;  //没有前一个node
        init_node.acton = Action.DONOTHING;    //没有动作
        init_node.path_cost = 0;    //路径花费自然也为零

        //System.out.println("初始"+init_node);

        //将初始节点加入frontier以便开始循环
        frontier.add(init_node);
        //算法核心阶段，详见 算法思想.txt
        while(true){
            if(frontier.isEmpty()){
                System.out.println("此迷宫无解(no solution)");
                return;
            }else{
                //判断激发函数值
                int heuritic = Integer.MAX_VALUE;
                Node temp = new Node();
                temp = frontier.get(0);
                int flag = 0;
                
                //遍历找出最小的激活值
                for(int i =0; i < frontier.size(); i++){
                    //System.out.println("frontier里面的有"+frontier.get(i));

                    int x = frontier.get(i).cur_state.x_pos;
                    int y = frontier.get(i).cur_state.y_pos;
                    int gn = frontier.get(i).cur_state.matrix[x][y].Manhattan_Distance;
                    int hn = frontier.get(i).path_cost;

                    if(heuritic > (hn + gn) || heuritic == (hn + gn)){
                        //更新更小的激活值
                        heuritic = (hn + gn);
                        flag = i;
                    }
                }
                
                //计算得出下一步要走的节点，并把它从frontier中移除
                temp = frontier.get(flag);
                frontier.remove(flag);

                //System.out.println("弹出的是"+temp);
                
                //完成了一步
                count ++;
                //如果觉得太多，不要看每一步，怎么走的就把这一行注释掉
                output(temp.cur_state.matrix,BARRIER);System.out.println("\n");
                
                //把temp加入以探索节点
                exploered.add(temp);

                //System.out.println("以探索的有"+exploered);

                //如果这个状态是终点,返回result
                if(temp.cur_state.x_pos == final_state.x_pos &&
                    temp.cur_state.y_pos == final_state.y_pos){
                        temp.cur_state.matrix[final_state.x_pos][final_state.y_pos].signal = END;
                        output(temp.cur_state.matrix, BARRIER);
                        System.out.println("\n");
                        System.out.println("AI通过" + count + "次尝试走出了迷宫");
                        System.out.println("有效路径如下：");
                        traceBack(temp);
                        return;
                }else{//否则调用扩展，扩展可到达节点
                    Node n = new Node();
                    for(int i = Action.UP; i <= Action.RIGHT; i++){
                        n = result(temp, i);
                        //能走通，而且未探索过
                        if(n != null && !exploered.contains(n)){
                            frontier.add(n);
                            //System.out.println("加入了"+n);
                        }
                    }
                }
            }
        }
    }

    //javaIO操作，不太懂的朋友自行补课
    public static State input(String FileName, int type) throws IOException {
        State state = new State();
        Value[][] maze = new Value[100][100];
        
        String root = System.getProperty("user.dir");
        String filePath = root + File.separator + "text" + File.separator + FileName;
    
        FileReader reader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(reader);
    
        if (!reader.ready()) {
            System.out.println("文件内容为空");
            br.close();
            return null;
        }else{
            String line = null;
            int row = 0;
            int column = 0;
            //一行一行读
            while((line = br.readLine()) != null){
                while(column < line.length()){
                    maze[row][column] = new Value();
                    if(line.charAt(column) == ' '){
                        maze[row][column].signal = SPACE;
                    } 
                    else if(line.charAt(column) == 'A'){
                        maze[row][column].signal = START;
                        if(type == START){
                            //标记起点坐标
                            state.x_pos = row;
                            state.y_pos = column;
                        }                      
                    }                       
                    else if(line.charAt(column) == 'B'){
                        maze[row][column].signal = END;
                        if(type == END){
                            //标记起点坐标
                            state.x_pos = row;
                            state.y_pos = column;
                        }           
                    }                      
                    else if(line.charAt(column) == '#'){
                        maze[row][column].signal = BARRIER;
                    }
                    
                    column ++;
                }
                column = 0;
                row ++;
            }
            br.close();
        }
        //如果是初始状态，返回一个二维矩阵和起点坐标
        //如果是终止状态，返回终点坐标和空矩阵
        if(type == START)
            state.matrix = maze;
        if(type == END)
            state.matrix = null;
        return state;
    }

    //计算整个状态图每个点的曼哈顿距离
    public static void manhattanSet(State state, int x_goal, int y_goal){
        int lenx = state.matrix.length;
        
        //遍历数组，计算每一个格子的曼哈顿距离
        for(int i = 0; i < lenx; i++){
            for(int j =0; j < state.matrix[i].length; j++){
                if(state.matrix[i][j] != null && state.matrix[i][j].signal !=BARRIER){
                    state.matrix[i][j].Manhattan_Distance = Math.abs(x_goal-i) + Math.abs(y_goal-j);
                }  
            }
        }
    }

    //通过pre_node成员变量回溯路径的方法
    public static void traceBack(Node fianl_node){
        Node tmp = new Node();
        tmp = fianl_node.pre_node;
        int x=0; int y=0;

        while(tmp.pre_node != null){
            //记录路径坐标
            x = tmp.cur_state.x_pos;
            y = tmp.cur_state.y_pos;
            //向前推近一个，类似链表
            tmp = tmp.pre_node;
            //将这个点涂黑
            fianl_node.cur_state.matrix[x][y].signal = PATH;
        }
        //输出一下
        output(fianl_node.cur_state.matrix, WALL);
    }

    //简单的遍历输出
    public static void output(Value[][] maze,int type){
        for(int i = 0; i < maze.length-1; i ++){
            for(int j = 0; j < maze[i].length; j ++){
                if(maze[i][j] != null){
                    if(maze[i][j].signal == BARRIER && type == BARRIER)
                        System.out.print("■");
                    if(maze[i][j].signal == BARRIER && type == WALL)
                        System.out.print("□");
                    if(maze[i][j].signal== START)
                        System.out.print("起");
                    if(maze[i][j].signal == END)
                        System.out.print("终");
                    if(maze[i][j].signal == SPACE)
                        System.out.print("  ");
                    if(maze[i][j].signal == PASSED)
                        System.out.print("○");
                    if(maze[i][j].signal == PATH)
                        System.out.print("●");
                }else{
                    break;
                } 
            }
            if(maze[i+1][0] != null)
                System.out.print("\n");
        }
    }

    //计算扩展节点的函数，或者说计算下一步可以怎么走
    public static Node result(Node cur_node, int acton){
        Node next = new Node();
        //用state代替cur_node.cur_state
        //看着舒服
        State state = new State();
        state = cur_node.cur_state;

        //这里不能直接等于cur_State，如果直接用就会传指针了，原状态的值也会跟着改
        /**
         * 1、基本类型作为参数传递时，是传递值的拷贝，无论你怎么改变这个拷贝，原值是不会改变的
         *
         * 2、对象作为参数传递时，是把对象在内存中的地址拷贝了一份传给了参数。
         */
        next.cur_state = new State();
        next.cur_state.matrix = new Value[100][100];
        for(int i =0; i < state.matrix.length; i++){
            for(int j =0; j < state.matrix[i].length; j++){
                if(state.matrix[i][j] == null){
                    break;
                }
                next.cur_state.matrix[i][j] = new Value();
                //传曼哈顿距离
                next.cur_state.matrix[i][j].Manhattan_Distance = 
                state.matrix[i][j].Manhattan_Distance;

                //传保存值
                next.cur_state.matrix[i][j].signal = 
                state.matrix[i][j].signal;
            }
        }
        //传当前x，y坐标
        next.cur_state.x_pos = state.x_pos;
        next.cur_state.y_pos = state.y_pos;

        //这里只注释一个动作，后面同理
        if(acton == Action.UP){
            //如果正上方的是空格或者是终点，因为打印的时候要区分终点，所以这里就把终点和空格都写进来
            if(state.matrix[state.x_pos-1][state.y_pos].signal == SPACE
            || state.matrix[state.x_pos-1][state.y_pos].signal == END){
                //System.out.println("goup");
                //保存值设为PASSED
                next.cur_state.matrix[state.x_pos-1][state.y_pos].signal = PASSED;
                //因为向上走了，所以行数减一，这里x不是横轴坐标，不要当成数轴了
                next.cur_state.x_pos -=1;
                //原来节点的索引保留下来，类似链表
                next.pre_node = cur_node;//原来的
                //记录动作
                next.acton = Action.UP;
                //记录开销
                next.path_cost ++;
                //如果上面能走通就直接返回了，代表一个动作的完成
                return next;
            }
        }
        if(acton == Action.LEFT){
            if(state.matrix[state.x_pos][state.y_pos-1].signal == SPACE
            || state.matrix[state.x_pos][state.y_pos-1].signal == END){
                //System.out.println("goleft");
                next.cur_state.matrix[state.x_pos][state.y_pos-1].signal = PASSED;
                next.cur_state.y_pos-=1;

                next.pre_node = cur_node;//原来的
                
                next.acton = Action.LEFT;
                next.path_cost ++;
                return next;
            }
        }
        if(acton == Action.RIGHT){
            if(state.matrix[state.x_pos][state.y_pos+1].signal == SPACE
            || state.matrix[state.x_pos][state.y_pos+1].signal == END){
                //System.out.println("goright");
                next.cur_state.matrix[state.x_pos][state.y_pos+1].signal = PASSED;
                next.cur_state.y_pos +=1;

                next.pre_node = cur_node;//原来的
                
                next.acton = Action.RIGHT;
                next.path_cost ++;
                return next;
            }
        }
        if(acton == Action.DOWN){
            if(state.matrix[state.x_pos+1][state.y_pos].signal == SPACE
            || state.matrix[state.x_pos+1][state.y_pos].signal == END){
                //System.out.println("godown");
                next.cur_state.matrix[state.x_pos+1][state.y_pos].signal = PASSED;
                next.cur_state.x_pos +=1;

                next.pre_node = cur_node;//原来的
                
                next.acton = Action.DOWN;
                next.path_cost ++;
                return next;
            }
        }
        //上下左右都堵死了，返回空
        return null;
    }
}

