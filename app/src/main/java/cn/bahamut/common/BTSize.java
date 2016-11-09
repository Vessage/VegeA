package cn.bahamut.common;

/**
 * Created by alexchow on 2016/11/4.
 */

public class BTSize {
    static public final BTSize ZERO = new BTSize(0,0);
    public float width;
    public float height;

    public BTSize(int width,int height){
        this.width = width;
        this.height = height;
    }

    public BTSize(float width,float height){
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return String.format("BTSize(%.1f,%.1f)",width,height);
    }

    public int getHeightInt() {
        return (int)height;
    }

    public int getWidthInt() {
        return (int)width;
    }
}
