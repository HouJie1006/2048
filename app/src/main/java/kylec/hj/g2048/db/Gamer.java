package kylec.hj.g2048.db;

/**
 * 游戏排行记录
 */
public class Gamer {

    private Integer id;

    private String name;

    private int score;
    private String time;

    public Gamer(Integer id, String name, int score, String time) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}
