package app.allclear.platform.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicUpdate;

import app.allclear.platform.value.FacilityValue;
import app.allclear.platform.value.PeopleValue;

/**********************************************************************************
*
*	Entity Bean CMP class that represents the people_facility table.
*
*	@author smalleyd
*	@version 1.0.108
*	@since April 13, 2020
*
**********************************************************************************/

@Entity
@Cacheable
@DynamicUpdate
@Table(name="people_facility")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="people_facility")
@NamedQueries({@NamedQuery(name="findPeopleFacility", query="SELECT OBJECT(o) FROM PeopleFacility o WHERE o.personId = :personId AND o.facilityId = :facilityId"),	// In lieu of creating a PK class. Not needed since not cached. DLS on 4/13/2020.
	@NamedQuery(name="getFacilityIdsByPerson", query="SELECT o.facilityId FROM PeopleFacility o WHERE o.personId = :personId")})
public class PeopleFacility implements Serializable
{
	private final static long serialVersionUID = 1L;

	@Column(name="person_id", columnDefinition="VARCHAR(10)", nullable=false)
	@Id
	public String getPersonId() { return personId; }
	public String personId;
	public void setPersonId(final String newValue) { personId = newValue; }

	@Column(name="facility_id", columnDefinition="BIGINT", nullable=false)
	@Id
	public Long getFacilityId() { return facilityId; }
	public Long facilityId;
	public void setFacilityId(final Long newValue) { facilityId = newValue; }

	@Column(name="created_at", columnDefinition="DATETIME", nullable=false)
	public Date getCreatedAt() { return createdAt; }
	public Date createdAt;
	public void setCreatedAt(final Date newValue) { createdAt = newValue; }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="person_id", nullable=false, updatable=false, insertable=false)
	public People getPerson() { return person; }
	public People person;
	public void setPerson(final People newValue) { person = newValue; }
	public void putPerson(final People newValue) { personId = (person = newValue).getId(); }

	@ManyToOne(cascade={}, fetch=FetchType.LAZY)
	@JoinColumn(name="facility_id", nullable=false, updatable=false, insertable=false)
	public Facility getFacility() { return facility; }
	public Facility facility;
	public void setFacility(final Facility newValue) { facility = newValue; }
	public void putFacility(final Facility newValue) { facilityId = (facility = newValue).getId(); }

	public PeopleFacility() {}

	public PeopleFacility(final People person,
		final Facility facility,
		final Date createdAt)
	{
		this.personId = (this.person = person).getId();
		this.facilityId = (this.facility = facility).getId();
		this.createdAt = createdAt;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (!(o instanceof PeopleFacility)) return false;

		var v = (PeopleFacility) o;
		return Objects.equals(personId, v.personId) &&
			Objects.equals(facilityId, v.facilityId) &&
			DateUtils.truncatedEquals(createdAt, v.createdAt, Calendar.SECOND);
	}

	@Transient
	public PeopleValue toPerson() { return getPerson().toValue(); }

	@Transient
	public FacilityValue toFacility() { return getFacility().toValue(); }
}
