package uk.gov.register.presentation.dao;

import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class PGObjectFactory {
    public static PGobject jsonbObject(String value){
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(value);
            return pGobject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
