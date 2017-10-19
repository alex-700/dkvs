package algorithm.messages.replica;

import java.util.Objects;

public class SetCommand extends Command {
    public int id;
    public String key;
    public String value;

    public SetCommand(int id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetCommand)) return false;
        SetCommand that = (SetCommand) o;
        return id == that.id &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, value);
    }

    @Override
    public String toString() {
        return "SET " + key + " " + value + " id= " + id;
    }
}
