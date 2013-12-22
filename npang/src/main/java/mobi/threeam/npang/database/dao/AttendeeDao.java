package mobi.threeam.npang.database.dao;


import java.sql.SQLException;

import mobi.threeam.npang.database.model.Attendee;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class AttendeeDao extends BaseDaoImpl<Attendee, Long> {

	public AttendeeDao(ConnectionSource connectionSource, Class<Attendee> dataClass)
			throws SQLException {
		super(connectionSource, dataClass);
	}

	public AttendeeDao(ConnectionSource connectionSource,
			DatabaseTableConfig<Attendee> tableConfig) throws SQLException {
		super(connectionSource, tableConfig);
	}
	
	

}
