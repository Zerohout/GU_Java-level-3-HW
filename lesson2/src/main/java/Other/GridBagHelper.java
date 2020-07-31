package Other;

import javax.swing.*;
import java.awt.*;

public class GridBagHelper {
    // координаты текущей ячейки
    private int gridX, gridY;

    // настраиваемый объект GridBagConstraints
    private GridBagConstraints constraints;

    public GridBagHelper() {
        constraints = new GridBagConstraints();
    }

    public void reset(){
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0.0f;
        constraints.weighty = 0.0f;
        constraints.insets = new Insets(0,0,0,0);
    }

    // возвращает настроенный объект GridBagConstraints
    public GridBagConstraints get() {
        return constraints;
    }

    // двигается на следующую ячейку
    public GridBagHelper nextCell() {
        constraints.gridx = gridX++;
        constraints.gridy = gridY;
        // для удобства возвращаем себя
        return this;
    }

    // двигается на следующий ряд
    public GridBagHelper nextRow() {
        gridX = 0;
        gridY++;
        constraints.gridx = 0;
        constraints.gridy = gridY;
        return this;
    }

    // заполнить весь ряд
    public GridBagHelper spanX() {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }

    // заполнить по вертикали
    public GridBagHelper spanY() {
        constraints.gridheight = GridBagConstraints.REMAINDER;
        return this;
    }

    // заполняет ячейку по горизонтали
    public GridBagHelper fillHorizontally() {
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    // вставляет распорку справа
    public GridBagHelper gap(int size) {
        constraints.insets.right = size;
        return this;
    }

    public GridBagHelper fillBoth() {
        constraints.fill = GridBagConstraints.BOTH;
        return this;
    }

    public GridBagHelper alignLeft() {
        constraints.anchor = GridBagConstraints.LINE_START;
        return this;
    }

    public GridBagHelper alignRight() {
        constraints.anchor = GridBagConstraints.LINE_END;
        return this;
    }

    public GridBagHelper setInsets(int left, int top, int right, int bottom) {
        Insets i = new Insets(top, left, bottom, right);
        constraints.insets = i;
        return this;
    }

    public GridBagHelper setWeights(float horizontal, float vertical) {
        constraints.weightx = horizontal;
        constraints.weighty = vertical;
        return this;
    }

    public void insertEmptyRow(Container c, int height) {
        Component comp = Box.createVerticalStrut(height);
        nextCell().nextRow().fillHorizontally().spanX();
        c.add(comp, get());
        nextRow();
    }

    public void insertEmptyFiller(Container c) {
        Component comp = Box.createGlue();
        nextCell().nextRow().fillBoth().spanX().spanY().setWeights(1.0f, 1.0f);
        c.add(comp, get());
        nextRow();
    }

    public void insertEmptyCell(Container c, int gridX, int gridY, int w, int h){
        Component comp = Box.createGlue();
        constraints.gridx = gridX;
        constraints.gridy = gridY;
        constraints.gridwidth = w;
        constraints.gridheight = h;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;

        c.add(comp,get());
        reset();
    }

    public GridBagHelper joinCells(int gridWidth, int gridHeight){
        constraints.gridwidth = gridWidth;
        constraints.gridheight = gridHeight;
        return this;
    }

    public GridBagHelper setLocation(int gridX, int gridY){
        constraints.gridx = gridX;
        constraints.gridy = gridY;
        return this;
    }
}
