package uk.gov.register.functional.app;

public class RsfRegisterDefinition {

    public static String ADDRESS_NAME = "add-item\t{\"register-name\":\"address\"}\n" +
            "append-entry\tsystem\tregister-name\t2017-06-01T10:00:00Z\tsha-256:50e3d51c16e203c0124e8bf3a8807abc7693f0d01cb0569499c608f98d2924e9\n";

    public static String ADDRESS_REGISTER =
        "add-item\t{\"fields\":[\"address\",\"street\",\"locality\",\"town\",\"area\",\"postcode\",\"country\",\"latitude\",\"longitude\"],\"phase\":\"alpha\",\"register\":\"address\",\"registry\":\"office-for-national-statistics\",\"text\":\"Register of addresses\"}\n" +
        "append-entry\tsystem\tregister:address\t2017-06-06T09:54:11Z\tsha-256:2f90a43858c366134a070f563697e04a851c977cd27e491c02885a2f4441e190\n";

    public static String ADDRESS_FIELDS = "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"extra\",\"phase\":\"alpha\",\"text\":\"extra field to make the test fail initially\"}\n" +
            "append-entry\tsystem\tfield:extra\t2017-06-09T12:59:51Z\tsha-256:bca2e5228ff9ebc8a2b4553afb46d51dbdf74f2484527c8897df90cbaaa00514\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"address\",\"phase\":\"alpha\",\"register\":\"address\",\"text\":\"A place in the UK with a postal address.\"}\n" +
            "append-entry\tsystem\tfield:address\t2017-06-09T12:59:51Z\tsha-256:cf5700d23d4cd933574fbafb48ba6ace1c3b374b931a6183eeefab6f37106011\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"street\",\"phase\":\"alpha\",\"text\":\"The number and street name of an address.\"}\n" +
            "append-entry\tsystem\tfield:street\t2017-06-09T12:59:51Z\tsha-256:6f3c85090641b9a6b153681de23fa0c7c8ad7ae2cc67acb51bcbb6ba88453b7e\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"locality\",\"phase\":\"alpha\",\"text\":\"The area within a post town.\"}\n" +
            "append-entry\tsystem\tfield:locality\t2017-06-09T12:59:51Z\tsha-256:4b21f41f73f1483b4be35171904cbe7adeb2256df14f384d91ff70d4c24e8199\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"town\",\"phase\":\"alpha\",\"text\":\"The town of an address.\"}\n" +
            "append-entry\tsystem\tfield:town\t2017-06-09T12:59:51Z\tsha-256:66e63a0bf090589c2c6b6f04bb00b96344a3fed81dd0bd2696c762ed7029ddc3\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"area\",\"phase\":\"alpha\",\"text\":\"The administrative area of an address.\"}\n" +
            "append-entry\tsystem\tfield:area\t2017-06-09T12:59:51Z\tsha-256:d8b50e38c1d16dc7abfa84329a5b43009b2b0c6ff0edd2cdd3d9f28b789c5767\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"register\":\"postcode\",\"text\":\"UK Postcodes.\"}\n" +
            "append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:bc2ac1d18b2172c19372fba27308ed05554fdc6f9d938cff1c903b86313bbab8\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"country\",\"phase\":\"alpha\",\"register\":\"country\",\"text\":\"ISO 3166-2 two letter code for a country.\"}\n" +
            "append-entry\tsystem\tfield:country\t2017-06-09T12:59:51Z\tsha-256:a77c35a853ea8adce1922cadd664fe6548a7b34e322e53f8d28517ee22138ee5\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"latitude\",\"phase\":\"alpha\",\"text\":\"Latitude of a place.\"}\n" +
            "append-entry\tsystem\tfield:latitude\t2017-06-09T12:59:51Z\tsha-256:52c98b6782a4631243970b55535cc6b90eb236006ee80a64bd8531d075a9065f\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"longitude\",\"phase\":\"alpha\",\"text\":\"Longitude of a place.\"}\n" +
            "append-entry\tsystem\tfield:longitude\t2017-06-09T12:59:51Z\tsha-256:105621a1707510d16be14b6a5a11347eda4dab0314a9d9a5f89a50602f7b71c6\n";

    public static String POSTCODE_REGISTER =
        "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
        "append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a\n" +
        "add-item\t{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
        "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:323fb3d9167d55ea8173172d756ddbc653292f8debbb13f251f7057d5cb5e450\n";

    public static String REGISTER_REGISTER =
        "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"register\",\"phase\":\"alpha\",\"register\":\"register\",\"text\":\"A register name.\"}\n" +
        "append-entry\tsystem\tfield:register\t2017-06-09T12:59:51Z\tsha-256:955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0\n" +
        "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"text\",\"phase\":\"alpha\",\"text\":\"Description of register entry.\"}\n" +
        "append-entry\tsystem\tfield:text\t2017-06-09T12:59:51Z\tsha-256:243a2dafca693363f99c38487a03d1a241915c47a38ad5627ad941c9e52b4c7b\n" +
        "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"registry\",\"phase\":\"alpha\",\"text\":\"Body responsible for maintaining one or more registers\"}\n" +
        "append-entry\tsystem\tfield:registry\t2017-06-09T12:59:51Z\tsha-256:4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2\n" +
        "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"phase\",\"phase\":\"alpha\",\"text\":\"Phase of a register or service as defined by the [digital service manual](https://www.gov.uk/service-manual).\"}\n" +
        "append-entry\tsystem\tfield:phase\t2017-06-09T12:59:51Z\tsha-256:1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4\n" +
        "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"copyright\",\"phase\":\"alpha\",\"text\":\"Copyright for the data in the register.\"}\n" +
        "append-entry\tsystem\tfield:copyright\t2017-06-09T12:59:51Z\tsha-256:ecbbde36c6a9808b5f116c63f9ca14773ac3fac251b53e21a1d9fd4b2dd1b35c\n" +
        "add-item\t{\"cardinality\":\"n\",\"datatype\":\"string\",\"field\":\"fields\",\"phase\":\"alpha\",\"register\":\"field\",\"text\":\"Set of field names.\"}\n" +
        "append-entry\tsystem\tfield:fields\t2017-06-09T12:59:51Z\tsha-256:61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26\n" +
        "add-item\t{\"fields\":[\"register\",\"text\",\"registry\",\"phase\",\"copyright\",\"fields\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of registers\"}\n" +
        "append-entry\tsystem\tregister:register\t2017-06-06T09:54:11Z\tsha-256:2238e546c1d9e81a3715d10949dedced0311f596304fbf9bb48c50833f8ab025\n";
}

