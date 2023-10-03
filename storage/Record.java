package storage;

public class Record {
    public static final int size = 37;
    public String GAME_DATE_EST;
    public int TEAM_ID_home;
    public int PTS_home;
    public float FG_PCT_home;
    public float FT_PCT_home;
    public float FG3_PCT_home;
    public int AST_home;
    public int REB_home;
    public boolean HOME_TEAM_WINS;

    public Record(String GAME_DATE_EST, int TEAM_ID_home, int PTS_home, float FG_PCT_home, float FT_PCT_home, float FG3_PCT_home, int AST_home, int REB_home, boolean HOME_TEAM_WINS) {
        this.GAME_DATE_EST = GAME_DATE_EST;
        this.TEAM_ID_home = TEAM_ID_home;
        this.PTS_home = PTS_home;
        this.FG_PCT_home = FG_PCT_home;
        this.FT_PCT_home = FT_PCT_home;
        this.FG3_PCT_home = FG3_PCT_home;
        this.AST_home = AST_home;
        this.REB_home = REB_home;
        this.HOME_TEAM_WINS = HOME_TEAM_WINS;
    }
}