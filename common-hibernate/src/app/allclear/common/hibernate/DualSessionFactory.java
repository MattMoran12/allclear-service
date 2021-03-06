package app.allclear.common.hibernate;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.*;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

/** Represents a wrapper/decorator class for a Hibernate session factory that can manage
 *  both a primary session factory and a read-only session factory.
 *
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */
@SuppressWarnings({"deprecation", "rawtypes"})
public class DualSessionFactory implements SessionFactory
{
	private static final long serialVersionUID = 1L;

	private final SessionFactory primary;
	private final SessionFactory reader;
	private final ThreadLocal<SessionFactory> current = new ThreadLocal<SessionFactory>();

	public DualSessionFactory(final SessionFactory primary, final SessionFactory reader)
	{
		this.primary = primary;
		this.reader = reader;
		this.current.set(primary);	// Main thread should use primary.
	}

	public DualSessionFactory(final SessionFactory only)	// For tests
	{
		this(only, only);
	}

	/** Activates either the primary or the reader session factory depending on the readOnly parameter.
	 * 
	 * @param readOnly
	 * @return the session factory in use
	 */
	public SessionFactory prepare(final boolean readOnly)
	{
		var factory = readOnly ? reader : primary;
		current.set(factory);

		return factory;
	}

	public SessionFactory current() { return current.get(); }

	@Override
	public EntityManager createEntityManager() { return current().createEntityManager(); }

	@Override
	public EntityManager createEntityManager(final Map map) { return current().createEntityManager(map); }

	@Override
	public EntityManager createEntityManager(final SynchronizationType synchronizationType) { return current().createEntityManager(synchronizationType); }

	@Override
	public EntityManager createEntityManager(final SynchronizationType synchronizationType, final Map map) { return current().createEntityManager(synchronizationType, map); }

	@Override
	public CriteriaBuilder getCriteriaBuilder() { return current().getCriteriaBuilder(); }

	@Override
	public boolean isOpen() { return current().isOpen(); }

	@Override
	public Map<String, Object> getProperties() { return current().getProperties(); }

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() { return current().getPersistenceUnitUtil(); }

	@Override
	public void addNamedQuery(String name, Query query) { current().addNamedQuery(name, query); }

	@Override
	public <T> T unwrap(Class<T> cls) { return current().unwrap(cls); }

	@Override
	public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) { current().addNamedEntityGraph(graphName, entityGraph); }

	@Override
	public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) { return current().findEntityGraphsByType(entityClass); }

	@Override
	public Metamodel getMetamodel() { return current().getMetamodel(); }

	@Override
	public Reference getReference() throws NamingException { return current().getReference(); }

	@Override
	public SessionFactoryOptions getSessionFactoryOptions() { return current().getSessionFactoryOptions(); }

	@Override
	public SessionBuilder withOptions() { return current().withOptions(); }

	@Override
	public Session openSession() throws HibernateException { return current().openSession(); }

	@Override
	public Session getCurrentSession() throws HibernateException { return current().getCurrentSession(); }

	@Override
	public StatelessSessionBuilder withStatelessOptions() { return current().withStatelessOptions(); }

	@Override
	public StatelessSession openStatelessSession() { return current().openStatelessSession(); }

	@Override
	public StatelessSession openStatelessSession(Connection connection) { return current().openStatelessSession(connection); }

	@Override
	public Statistics getStatistics() { return current().getStatistics(); }

	@Override
	public void close() throws HibernateException { current().close(); }

	@Override
	public boolean isClosed() { return current().isClosed(); }

	@Override
	public Cache getCache() { return current().getCache(); }

	@Override
	public Set getDefinedFilterNames() { return current().getDefinedFilterNames(); }

	@Override
	public FilterDefinition getFilterDefinition(String filterName) throws HibernateException { return current().getFilterDefinition(filterName); }

	@Override
	public boolean containsFetchProfileDefinition(String name) { return current().containsFetchProfileDefinition(name); }

	@Override
	public TypeHelper getTypeHelper() { return current().getTypeHelper(); }

	@Override
	public ClassMetadata getClassMetadata(Class entityClass) { return current().getClassMetadata(entityClass); }

	@Override
	public ClassMetadata getClassMetadata(String entityName) { return current().getClassMetadata(entityName); }

	@Override
	public CollectionMetadata getCollectionMetadata(String roleName) { return current().getCollectionMetadata(roleName); }

	@Override
	public Map<String, ClassMetadata> getAllClassMetadata() { return current().getAllClassMetadata(); }

	@Override
	public Map getAllCollectionMetadata() { return current().getAllCollectionMetadata(); }

	public Dialect getDialect() { return ((SessionFactoryImpl) current()).getDialect(); }
	public boolean isH2Dialect() { return getDialect() instanceof H2Dialect; }
}
