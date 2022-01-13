package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import com.robotikflow.core.models.entities.App;
import com.robotikflow.core.models.entities.AppAuth;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;

public interface AppAuthRepository 
    extends JpaRepository<AppAuth, Long>
{
    @Query("select aa" +
    "           from AppAuth aa" +
    "           where aa.app = :app"
    )
	List<AppAuth> findAllByPage(
        App app, 
        Pageable appable);

    @Query("select aa" +
    "           from AppAuth aa" +
    "           where aa.pubId = :pubId"
    )
    AppAuth findByPubId(
        String pubId);

    @Query("select aa" +
    "           from AppAuth aa" +
    "           where aa.pubId = :pubId" +
    "                   and aa.app = :app"
    )
	AppAuth findByPubIdAndPage(
        String pubId, 
        App app);

	@Query("select" +
			"	aa" +
			"	from AppAuth aa" +
			"	where" +
            "       (aa.app = :app" +
			"			and aa.user = :user" +
			"			and aa.reverse = false" +
            "       )" +
			"		or" +
            "       (aa.app = :app" +
			"			and aa.group.id in" + 
            "               (select ga.child.id " + 
			"				    from GroupTree ga" + 
			"					inner join UserGroupWorkspace urg" + 
			"			 	    	on urg.group = ga.parent" + 
			"			 			    and urg.workspace = :workspace" + 
			"			 			    and urg.user = :user" +
			"   			)" +
			"			and aa.reverse = false" +
			"			and not exists" +
            "               (select cuprev.id" +
            "                   from AppAuth cuprev" +
			"					where cuprev.app = :app" +
			"						and cuprev.group = aa.group" +
			"						and cuprev.reverse = true" +		
            "               )" +
			"		    and not exists" +
            "              (select cuprev.id" +
			"			        from AppAuth cuprev" +
			"			        where cuprev.app = :app" +
			"		    		    and cuprev.user = :user" +
			"			    		and cuprev.reverse = true" +
			"		       )" +
			"		)"			
	)
	List<AppAuth> findAllByPageAndUserAndWorkspace(
        App app, 
        User user, 
        Workspace workspace);    
}
