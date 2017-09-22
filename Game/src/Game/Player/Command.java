package Game.Player;

public enum Command {
    Invalid("-1"),
    Refresh("0"),
    West("1"),
    South("2"),
    East("3"),
    North("4"),
    Exit("9");

    private String value;
    Command(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
