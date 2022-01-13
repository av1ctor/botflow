package com.robotikflow.core.config;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.robotikflow.core.RobotikflowCore;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	entityManagerFactoryRef = "robotikflowEntityManagerFactory",
    transactionManagerRef = "robotikflowTransactionManager", 
    basePackageClasses = {RobotikflowCore.class})
public class DbConfig
{
	@Bean("robotikflowDataSourceProperties")
	@Primary
	@ConfigurationProperties("app.datasource.robotikflow")
	public DataSourceProperties robotikflowDataSourceProperties() 
	{
		return new DataSourceProperties();
	}
	
	@Bean("robotikflowDataSource")
	@Primary
	@ConfigurationProperties("app.datasource.robotikflow.configuration")
	public HikariDataSource robotikflowBpmDataSource() 
	{
		return robotikflowDataSourceProperties().initializeDataSourceBuilder()
				.type(HikariDataSource.class).build();
	}

	protected Map<String, Object> jpaProperties() 
	{
	    var props = new HashMap<String, Object>();
	    props.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
	    props.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
	    return props;
	}
	
	@Bean(name = "robotikflowEntityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean robotikflowEntityManagerFactory(
			EntityManagerFactoryBuilder builder, @Qualifier("robotikflowDataSource") DataSource dataSource) 
	{
		return builder
				.dataSource(dataSource)
				.packages(RobotikflowCore.class)
				.persistenceUnit("robotikflow")
				.properties(jpaProperties())
				.build();
	}

	@Bean(name = "robotikflowTransactionManager")
	@Primary
	public PlatformTransactionManager robotikflowTransactionManager(
			@Qualifier("robotikflowEntityManagerFactory") EntityManagerFactory robotikflowEntityManagerFactory) 
	{
		return new JpaTransactionManager(robotikflowEntityManagerFactory);
	}
}
