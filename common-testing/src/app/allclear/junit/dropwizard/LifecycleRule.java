package app.allclear.junit.dropwizard;

import java.util.LinkedList;
import java.util.List;

import org.junit.rules.ExternalResource;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.*;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.Managed;

import io.dropwizard.testing.junit5.DropwizardExtension;

import com.codahale.metrics.MetricRegistry;

/** JUnit external resource that mimics the Dropwizard Lifecycle manager.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class LifecycleRule extends ExternalResource implements DropwizardExtension
{
	private static final Logger log = LoggerFactory.getLogger(LifecycleRule.class);
	private final List<Managed> managed;

	public LifecycleRule()
	{
		managed = new LinkedList<>();
	}

	/** Add one or more components to the list of Managed resource.
	 *  When added, a resource will be started. When the rule terminates,
	 *  all the managed components will be stopped.
	 *
	 * @param values
	 */
	public void manage(final Managed... values) throws Exception
	{
		for (var v : values)
		{
			v.start();
			managed.add(v);
		}
	}

	/** Accepts a DataSourceFactory, creates the ManagedDataSource, starts the data source,
	 *  adds the data source to the list of managed components, and returns a DBI object based
	 *  on the data source.
	 *
	 * @param factory
	 * @param name
	 * @return never NULL
	 * @throws Exception
	 */
	public Jdbi manageForDBI(final DataSourceFactory factory, final String name) throws Exception
	{
		var ds = factory.build(new MetricRegistry(), name);
		ds.start();
		managed.add(ds);

		return Jdbi.create(ds);
	}

	@Override
	public void before() { /** Not implemented but MUST be public for DropwizardExtension. */ }

	@Override
	public void after()
	{
		managed.forEach(m -> {
			try { m.stop(); }
			catch (final Exception ex) { log.error(ex.getMessage(), ex); }
		});
	}
}
