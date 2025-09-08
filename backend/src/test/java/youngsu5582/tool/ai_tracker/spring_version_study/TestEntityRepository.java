package youngsu5582.tool.ai_tracker.spring_version_study;

import org.springframework.data.jpa.repository.JpaRepository;
import youngsu5582.tool.ai_tracker.spring_version_study.HibernateTests.TestEntity;

interface TestEntityRepository extends JpaRepository<TestEntity, Long> {

}