package br.pucpr.authserver.courses

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class StudentService(private val studentRepository: StudentRepository) {
    private val log = LoggerFactory.getLogger(StudentService::class.java)

    fun create(name: String, email: String): Student {
        log.info("Cadastrando novo estudante: '$name' ($email)")
        return studentRepository.save(Student(name = name, email = email))
    }

    fun listAll(nameQuery: String?, sortBy: String = "name"): List<Student> {
        log.info("Listando estudantes. Filtro: '$nameQuery', Ordenação: $sortBy")
        val sort = Sort.by(sortBy)
        return if (nameQuery.isNullOrBlank()) {
            studentRepository.findAll(sort)
        } else {
            studentRepository.findByNameContainingIgnoreCase(nameQuery)
        }
    }

    fun findById(id: Long): Student {
        log.info("Buscando estudante ID: $id")
        return studentRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante com ID $id não encontrado.") }
    }

    fun update(id: Long, name: String, email: String): Student {
        log.info("Atualizando estudante ID: $id")
        val student = findById(id)
        student.name = name
        student.email = email
        return studentRepository.save(student)
    }

    fun delete(id: Long) {
        log.warn("Removendo estudante ID: $id")
        val student = findById(id)
        studentRepository.delete(student)
    }
}