package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionAuth;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;

public interface CollectionAuthRepository extends JpaRepository<CollectionAuth, Long>
{
    @Query("select cp" +
    "           from CollectionAuth cp" +
    "           where cp.collection = :collection"
    )
	List<CollectionAuth> findAllByCollection(
        Collection collection, 
        Pageable pageable);

    @Query("select cp" +
    "           from CollectionAuth cp" +
    "           where cp.pubId = :pubId"
    )
    CollectionAuth findByPubId(
        String pubId);

    @Query("select cp" +
    "           from CollectionAuth cp" +
    "           where cp.pubId = :pubId" +
    "                   and cp.collection = :collection"
    )
	CollectionAuth findByPubIdAndCollection(
        String pubId, 
        Collection collection);

	@Query("select" +
			"	cp" +
			"	from CollectionAuth cp" +
			"	where" +
            "       (cp.collection = :collection" +
			"			and cp.user = :user" +
			"			and cp.reverse = false" +
            "       )" +
			"		or" +
            "       (cp.collection = :collection" +
			"			and cp.group.id in" + 
            "               (select ga.child.id " + 
			"				    from GroupTree ga" + 
			"					inner join UserGroupWorkspace urg" + 
			"			 	    	on urg.group = ga.parent" + 
			"			 			    and urg.workspace = :workspace" + 
			"			 			    and urg.user = :user" +
			"   			)" +
			"			and cp.reverse = false" +
			"			and not exists" +
            "               (select cuprev.id" +
            "                   from CollectionAuth cuprev" +
			"					where cuprev.collection = :collection" +
			"						and cuprev.group = cp.group" +
			"						and cuprev.reverse = true" +		
            "               )" +
			"		    and not exists" +
            "              (select cuprev.id" +
			"			        from CollectionAuth cuprev" +
			"			        where cuprev.collection = :collection" +
			"		    		    and cuprev.user = :user" +
			"			    		and cuprev.reverse = true" +
			"		       )" +
			"		)"			
	)
	List<CollectionAuth> findAllByCollectionAndUserAndWorkspace(
        Collection collection, 
        User user, 
        Workspace workspace);    
}
