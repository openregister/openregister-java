package uk.gov.register.functional.app;

public class RsfRegisterDefinition {

    public static String ADDRESS_NAME = "add-item\t{\"name\":\"address\"}\n" +
            "append-entry\tsystem\tname\t2017-06-01T10:00:00Z\tsha-256:eb5064e317d2d673634e4a40782ab727573bd2075a82cf69c05af919d7518794\n";

    public static String ADDRESS_REGISTER =
            "add-item\t{\"copyright\":\"Contains Ordnance Survey data © Crown copyright & database right 2015\\n Contains Royal Mail data © Royal Mail copyright & database right 2015\\n\",\"fields\":[\"address\",\"street\",\"locality\",\"town\",\"area\",\"postcode\",\"country\",\"latitude\",\"longitude\",\"property\"],\"phase\":\"alpha\",\"register\":\"address\",\"registry\":\"office-for-national-statistics\",\"text\":\"Register of addresses\"}\n" +
            "append-entry\tsystem\tregister:address\t2017-06-06T09:54:11Z\tsha-256:b9f80885b11cbc9064970214387571ebfae6795f62bd79723163a3a91162537e\n";

    public static String ADDRESS_FIELDS = "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"address\",\"phase\":\"alpha\",\"register\":\"address\",\"text\":\"A place in the UK with a postal address.\"}\n" +
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
            "append-entry\tsystem\tfield:longitude\t2017-06-09T12:59:51Z\tsha-256:105621a1707510d16be14b6a5a11347eda4dab0314a9d9a5f89a50602f7b71c6\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"property\",\"phase\":\"alpha\",\"text\":\"A building, institution or house name in an address.\"}\n" +
            "append-entry\tsystem\tfield:property\t2017-06-09T12:59:51Z\tsha-256:b91ad25f9d6db4b2588ff1724c09e4bf18f13538862efdeb051c7b9c0a5f0eed\n";

    public static String POSTCODE_REGISTER =
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"register\":\"postcode\",\"text\":\"UK Postcodes.\"}\n" +
            "append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:bc2ac1d18b2172c19372fba27308ed05554fdc6f9d938cff1c903b86313bbab8\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"point\",\"field\":\"point\",\"phase\":\"alpha\",\"text\":\"A geographical point\"}\n" +
            "append-entry\tsystem\tfield:point\t2017-06-09T12:59:51Z\tsha-256:7f7f01febb44bada60e4a7a6642f5def6e93f28c043b59eec3a4ccaa44f4ad0b\n" +
            "add-item\t{\"copyright\":\"Contains National Statistics data © [Crown copyright and database right 2013](http://www.nationalarchives.gov.uk/doc/open-government-licence/),\\n Contains Ordnance Survey data © [Crown copyright and database right 2013](http://www.ordnancesurvey.co.uk/oswebsite/docs/licences/os-opendata-licence.pdf),\\n Contains Royal Mail data © [Royal Mail copyright and database right 2013](http://www.dfpni.gov.uk/lps/index/copyright_licensing_publishing.htm)\",\"fields\":[\"postcode\",\"point\"],\"phase\":\"alpha\",\"register\":\"postcode\",\"registry\":\"office-for-national-statistics\",\"text\":\"Register of postcodes\"}\n" +
            "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:0a63146b7817f399b91bbeb1261017f0a111109946b1ca62788a760bf5ccb447\n";

    public static String REGISTER_REGISTER =
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"register\",\"phase\":\"alpha\",\"register\":\"register\",\"text\":\"A register name.\"}\n" +
                    "append-entry\tsystem\tfield:register\t2017-06-09T12:59:51Z\tsha-256:955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0\n" +
                    "add-item\t{\"cardinality\":\"1\",\"datatype\":\"text\",\"field\":\"text\",\"phase\":\"alpha\",\"text\":\"Description of register entry.\"}\n" +
                    "append-entry\tsystem\tfield:text\t2017-06-09T12:59:51Z\tsha-256:ceae38992b310fba3ae77fd84e21cdb6838c90b36bcb558de02acd2f6589bd3f\n" +
                    "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"registry\",\"phase\":\"alpha\",\"text\":\"Body responsible for maintaining one or more registers\"}\n" +
                    "append-entry\tsystem\tfield:registry\t2017-06-09T12:59:51Z\tsha-256:4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2\n" +
                    "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"phase\",\"phase\":\"alpha\",\"text\":\"Phase of a register or service as defined by the [digital service manual](https://www.gov.uk/service-manual).\"}\n" +
                    "append-entry\tsystem\tfield:phase\t2017-06-09T12:59:51Z\tsha-256:1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4\n" +
                    "add-item\t{\"cardinality\":\"1\",\"datatype\":\"text\",\"field\":\"copyright\",\"phase\":\"alpha\",\"text\":\"Copyright for the data in the register.\"}\n" +
                    "append-entry\tsystem\tfield:copyright\t2017-06-09T12:59:51Z\tsha-256:c7e5a90c020f7686d9a275cb0cc164636745b10ae168a72538772692cc90d633\n" +
                    "add-item\t{\"cardinality\":\"n\",\"datatype\":\"string\",\"field\":\"fields\",\"phase\":\"alpha\",\"register\":\"field\",\"text\":\"Set of field names.\"}\n" +
                    "append-entry\tsystem\tfield:fields\t2017-06-09T12:59:51Z\tsha-256:61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26\n" +
                    "add-item\t{\"fields\":[\"register\",\"text\",\"registry\",\"phase\",\"copyright\",\"fields\"],\"phase\":\"alpha\",\"register\":\"register\",\"registry\":\"cabinet-office\",\"text\":\"Register of registers\"}\n" +
                    "append-entry\tsystem\tregister:register\t2017-06-06T09:54:11Z\tsha-256:f404b4739b51afeb39bba26f3bbf1aa8c6f7d25f0d54444992fc00f24587ef77\n";
}
