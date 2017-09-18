package uk.gov.register.views;

public class ItemSimpleView extends ItemView {

    private final String key;

    public ItemSimpleView(final String key, final ItemView itemView) {
        super(itemView.getItemHash(), itemView.getContent(), itemView.getFields());
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
