package uk.gov.register.functional.app;

public class RsfRegisterDefinition {
    public static String ADDRESS_REGISTER =
        "add-item\t{\"fields\":[\"address\",\"street\",\"locality\",\"town\",\"area\",\"postcode\",\"country\",\"latitude\",\"longitude\"],\"phase\":\"alpha\",\"register\":\"address\",\"registry\":\"office-for-national-statistics\",\"text\":\"Register of addresses\"}\n" +
        "append-entry\tsystem\tregister:address\t2017-06-06T09:54:11Z\tsha-256:2f90a43858c366134a070f563697e04a851c977cd27e491c02885a2f4441e190\n";

    public static String POSTCODE_REGISTER =
        "add-item\t{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
        "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:323fb3d9167d55ea8173172d756ddbc653292f8debbb13f251f7057d5cb5e450\n";

    public static String REGISTER_REGISTER =
        "add-item\t{\"fields\":[\"register\",\"text\",\"registry\",\"phase\",\"copyright\",\"fields\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of registers\"}\n" +
        "append-entry\tsystem\tregister:register\t2017-06-06T09:54:11Z\tsha-256:2238e546c1d9e81a3715d10949dedced0311f596304fbf9bb48c50833f8ab025\n";
}

