package sandkev.shared.dao;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Describe a database table
 * inspired by scm:git:git@github.com:nurkiewicz/spring-data-jdbc-repository.git
 */
public class TableDescription {

    private final String name;
   	private final List<String> idColumns;

   	public TableDescription(String name, String... idColumns) {
   		Assert.notNull(name);
   		Assert.notNull(idColumns);
   		Assert.isTrue(idColumns.length > 0, "At least one primary key column must be provided");

   		this.name = name;
   		this.idColumns = Collections.unmodifiableList(Arrays.asList(idColumns));
   	}

   	public String getName() {
   		return name;
   	}

   	public List<String> getIdColumns() {
   		return idColumns;
   	}
}
