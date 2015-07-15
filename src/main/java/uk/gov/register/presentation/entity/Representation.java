package uk.gov.register.presentation.entity;

public enum Representation{
    csv(new CsvEntity(",")),
    tsv(new CsvEntity("\t")),
    html(new HtmlEntity()),
    json(new JsonEntity());

    public final Entity entity;

    Representation(Entity entity) {
        this.entity = entity;
    }
}
