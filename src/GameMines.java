import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Timer;

class GameMines extends JFrame {//JFrame обеспечивает окошко на экране

    final String TITLE_OF_PROGRAM = "Mines";
    final String SIGN_OF_FLAG = "f";//Знак флага
    final int BLOCK_SIZE = 30;//Определение размера блока
    final int FIELD_SIZE = 9;//Размер поля
    final int FIELD_DX = 17;//Ширина рамок
    final int FIELD_DY = 38 + 17;//Ширина заголовка
    final int START_LOCATION = 200;//Стартовая поция открытия окна
    final int MOUSE_BUTTON_LEFT = 1;//Константа для нажатия кнопки мыши(кнопка для открытия поля)
    final int MOUSE_BUTTON_RIGHT = 3;//Константа для нажатия кнопки мыши(кнопка для отметки мины)
    final int NUMBER_OF_MINES = 10;//Кол-во мин
    final int[] COLOR_OF_NUMBERS = {0x0000FF,0x008000,0xFF0000,0x80000,0x0};//Массив с цветами
    Cell[][] field = new Cell[FIELD_SIZE][FIELD_SIZE];
    Random random = new Random();//Рандомное появление мин
    int countOpenedCells;//Хранение кол-ва открытых ячеек
    boolean youWon, bangMine;//Первое победа,второе взрыв\проигрыш
    int bangX, bangY;//Координаты взрыва

    public static void main(String[] arg){
        new GameMines();
    }

    GameMines(){
        setTitle(TITLE_OF_PROGRAM);//Устанавливает заголовок
        setDefaultCloseOperation(EXIT_ON_CLOSE);//Определяет закрытие окна и программы
        setBounds(START_LOCATION, START_LOCATION, FIELD_SIZE * BLOCK_SIZE + FIELD_DX, FIELD_SIZE * BLOCK_SIZE + FIELD_DY);//Устанавливает стартовую позицию окна и размеры
        setResizable(false);
        TimerLabel timeLabel = new TimerLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        final Canvas canvas = new Canvas();
        canvas.setBackground(Color.WHITE);//Цвет заднего фона
        canvas.addMouseListener(new MouseAdapter() { //Прослушиватель мыши
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX()/BLOCK_SIZE;
                int y = e.getY()/BLOCK_SIZE;
                if (!bangMine && !youWon) {
                    if (e.getButton() == MOUSE_BUTTON_LEFT) // left button mouse
                        if (field[y][x].isNotOpen()) {
                            openCells(x, y);

                            youWon = countOpenedCells == FIELD_SIZE*FIELD_SIZE - NUMBER_OF_MINES; // winning check
                            if (bangMine) {
                                bangX = x;
                                bangY = y;
                            }
                        }
                    if (e.getButton() == MOUSE_BUTTON_RIGHT) field[y][x].inverseFlag(); // right button mouse
                    if (bangMine || youWon) timeLabel.stopTimer(); // game over, становка таймера
                     canvas.repaint();
                }
            }
        });
        add(BorderLayout.CENTER, canvas);//поле игры
        add(BorderLayout.SOUTH, timeLabel);
        setVisible(true);//метод делвет окно видимым
        initField();
    }
    void openCells(int x, int y) { // recursive procedure of opening the cells
        if (x < 0 || x > FIELD_SIZE - 1 || y < 0 || y > FIELD_SIZE - 1) return; // wrong coordinates
        if (!field[y][x].isNotOpen()) return; // cell is already open
        field[y][x].open();
        if (field[y][x].getCountBomb() > 0 || bangMine) return; // the cell is not empty
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) openCells(x + dx, y + dy);
    }

    //Метод инициализации поля:
    void initField() { // initialization of the playing field
        int x, y, countMines = 0;
        // create cells for the field
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                field[y][x] = new Cell();
        // to mine field
        while (countMines < NUMBER_OF_MINES) {
            do {
                x = random.nextInt(FIELD_SIZE);
                y = random.nextInt(FIELD_SIZE);
            } while (field[y][x].isMined());
            field[y][x].mine();
            countMines++;
        }
        // to count dangerous neighbors
        for (x = 0; x < FIELD_SIZE; x++)
            for (y = 0; y < FIELD_SIZE; y++)
                if (!field[y][x].isMined()) {
                    int count = 0;
                    for (int dx = -1; dx < 2; dx++)
                        for (int dy = -1; dy < 2; dy++) {
                            int nX = x + dx;
                            int nY = y + dy;
                            if (nX < 0 || nY < 0 || nX > FIELD_SIZE - 1 || nY > FIELD_SIZE - 1) {
                                nX = x;
                                nY = y;
                            }
                            count += (field[nY][nX].isMined()) ? 1 : 0;
                        }
                    field[y][x].setCountBomb(count);
                }
    }

    class Cell {
        private boolean isOpen, isMine, isFlag;
        private int countBombNear;//Кол-во бомб вблизи

        void open(){
            isOpen = true;
            bangMine = isMine;//Если подрывается сапер
            if (!isMine) countOpenedCells++; //Если ячейка не заменирована,то тогда счетчик открытых ячеек увеличивается на 1

        }

        void mine(){
            isMine = true;//Метод минирования
        }

        void setCountBomb(int count){
            countBombNear = count;
        }

        int getCountBomb(){
            return countBombNear;
        }

        boolean isNotOpen(){ //Проверка открытия ячейки
            return !isOpen;
        }

        boolean isMined(){
            return isMine;
        }
        void inverseFlag(){
            isFlag = !isFlag;
        }



        void paintBomb(Graphics g, int x, int y, Color color) {//Прорисовка бомбы
            g.setColor(color);
            g.fillRect(x*BLOCK_SIZE + 7, y*BLOCK_SIZE + 10, 18, 10);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 6, 10, 18);
            g.fillRect(x*BLOCK_SIZE + 9, y*BLOCK_SIZE + 8, 14, 14);
            g.setColor(Color.white);
            g.fillRect(x*BLOCK_SIZE + 11, y*BLOCK_SIZE + 10, 4, 4);
        }

        void paintString(Graphics g, String str, int x, int y, Color color) {//метод рисования
            g.setColor(color);
            g.setFont(new Font("", Font.BOLD, BLOCK_SIZE));
            g.drawString(str, x*BLOCK_SIZE + 8, y*BLOCK_SIZE + 26);
        }

        void paint(Graphics g, int x, int y) {//
            g.setColor(Color.lightGray);
            g.drawRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
            if (!isOpen) {
                if ((bangMine || youWon) && isMine) paintBomb(g, x, y, Color.black);
                else {
                    g.setColor(Color.lightGray);
                    g.fill3DRect(x*BLOCK_SIZE, y*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE, true);
                    if (isFlag) paintString(g, SIGN_OF_FLAG, x, y, Color.red);
                }
            } else
            if (isMine) paintBomb(g, x, y, bangMine? Color.red : Color.black);
            else
            if (countBombNear > 0)
                paintString(g, Integer.toString(countBombNear), x, y, new Color(COLOR_OF_NUMBERS[countBombNear - 1]));
        }

    }

    static class TimerLabel extends JLabel { // label with stopwatch
        Timer timer = new Timer();

        TimerLabel() {
            timer.scheduleAtFixedRate(timerTask, 0, 1000);// TimerTask task, long delay, long period
        }

        TimerTask timerTask = new TimerTask() {
            volatile int time;
            final Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };
            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        void stopTimer() { timer.cancel(); }
    }
    
    //Класс псевдо конвы:
    class Canvas extends JPanel { // my canvas for painting
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int x = 0; x < FIELD_SIZE; x++)
                for (int y = 0; y < FIELD_SIZE; y++) field[y][x].paint(g, x, y);
        }
    }

    }




