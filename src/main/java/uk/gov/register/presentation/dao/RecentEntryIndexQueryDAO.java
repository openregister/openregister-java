package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.mapper.EntryMapper;

import java.util.List;

@RegisterMapper(EntryMapper.class)
public abstract class RecentEntryIndexQueryDAO {

}
