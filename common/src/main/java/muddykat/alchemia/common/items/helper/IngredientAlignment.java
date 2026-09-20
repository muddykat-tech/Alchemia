package muddykat.alchemia.common.items.helper;

public enum IngredientAlignment {
    Fire(-1, 0),
    Water(1, 0),
    Earth(0, 1),
    Air(0, -1),
    Void(0, 0);

    int x, y;
    IngredientAlignment(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
