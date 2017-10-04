package algorithm.messages.replica;

import java.util.Objects;

public class DeleteCommand extends Command {
    public int id;
    public String key;

    public DeleteCommand() {}

    public DeleteCommand(int id, String key) {
        this.id = id;
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeleteCommand that = (DeleteCommand) o;
        return id == that.id &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key);
    }

    @Override
    public String toString() {
        return String.format("DeleteCommand {id = %d, key = %s}", id, key);
    }
}