package mixit.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mixit.data.dto.MemberDataDto
import mixit.model.Event
import mixit.model.EventSponsoring
import mixit.support.getEntityInformation
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.repository.support.ReactiveMongoRepositoryFactory
import org.springframework.data.mongodb.repository.support.SimpleReactiveMongoRepository
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import reactor.core.publisher.Flux
import java.time.LocalDate

class EventRepository(db: ReactiveMongoTemplate, val userRepository: UserRepository, f: ReactiveMongoRepositoryFactory) :
        SimpleReactiveMongoRepository<Event, String>(f.getEntityInformation(Event::class), db) {


    fun initData() {
        deleteAll().block()

        val events = listOf(
                Event("mixit12", LocalDate.of(2012, 4, 26), LocalDate.of(2012, 4, 26), sponsors = readSponsorsForEvent(12)),
                Event("mixit13", LocalDate.of(2013, 4, 25), LocalDate.of(2013, 4, 26), sponsors = readSponsorsForEvent(13)),
                Event("mixit14", LocalDate.of(2014, 4, 29), LocalDate.of(2014, 4, 30), sponsors = readSponsorsForEvent(14)),
                Event("mixit15", LocalDate.of(2015, 4, 16), LocalDate.of(2015, 4, 17), sponsors = readSponsorsForEvent(15)),
                Event("mixit16", LocalDate.of(2016, 4, 21), LocalDate.of(2016, 4, 22), sponsors = readSponsorsForEvent(16)),
                Event("mixit17", LocalDate.of(2017, 4, 20), LocalDate.of(2017, 4, 21), true)
        )
        events.forEach { event -> save(event).block() }
    }

    /**
     * Loads data from the json sponsor files
     */
    fun readSponsorsForEvent(year: Int): List<EventSponsoring> {
        val file = ClassPathResource("data/sponsor/sponsor_mixit$year.json")
        val objectMapper: ObjectMapper = Jackson2ObjectMapperBuilder.json().build()
        var sponsors: List<MemberDataDto> = objectMapper.readValue(file.file)
        return sponsors.flatMap { sponsor -> sponsor.toEventSponsoring(userRepository.findOne("${sponsor.login}").block()) }
    }

    override fun findAll(): Flux<Event> = findAll(Sort("year"))


}
