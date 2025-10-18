package youngsu5582.tool.ai_tracker.common.exception;

public class EntityNotExistException extends RuntimeException {

    private final String entity;
    private final long id;

    public EntityNotExistException(Class<?> entity, long id) {
        super("Entity Not Found. Entity: %s, Id: %d".formatted(entity.getName(), id));
        this.entity = entity.getName();
        this.id = id;
    }

    public EntityNotExistException(String entity, long id) {
        super("Entity Not Found. Entity: %s, Id: %d".formatted(entity, id));
        this.entity = entity;
        this.id = id;
    }
}
