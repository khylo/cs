package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Spring Boot starter class
 */
@Slf4j
@SpringBootApplication
public class SpringBootProcessor {
	@Autowired
	JacksonJpaProcessor jacksonJpaProcessor;
	private static File file;


	/**
	 * main method for starting. Takes the filename
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		if(args.length<1){
			System.out.println("Usage SpringBootProcessor <filename>");
			System.exit(1);
		}
		file = new File(args[0]);
		if(!file.isFile()){
			System.out.println("Cannot find "+args[0]+ " from "+System.getProperty("user.dir"));
			System.exit(1);
		}
		SpringApplication app = new SpringApplication(SpringBootProcessor.class);

		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

	}


	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em
				= new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan(new String[] { "com.example.demo.model" });

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaProperties(additionalProperties());

		return em;
	}

	@Bean
	public DataSource dataSource(){
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
		dataSource.setUrl("jdbc:hsqldb:file:working/jsonDbFile");
		dataSource.setUsername( "sa" );
		dataSource.setPassword( "" );
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager(
			EntityManagerFactory emf){
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);

		return transactionManager;
	}

	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation(){
		return new PersistenceExceptionTranslationPostProcessor();
	}

	Properties additionalProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.hbm2ddl.auto", "update");
		properties.setProperty(
				"hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

		return properties;
	}
	/**
	 * Define the JacksonJpaProcessor bean that will do the work.
	 * Spring boot will call this method on after run is called.
	 * @return
	 * @throws IOException
	 */
	@Bean
	public CommandLineRunner runJacksonJpaProcessor() throws IOException {
		return (args) -> {
			log.info("Start processing json file");
			jacksonJpaProcessor.setFile(file);
			jacksonJpaProcessor.process();
		};
	}

}
