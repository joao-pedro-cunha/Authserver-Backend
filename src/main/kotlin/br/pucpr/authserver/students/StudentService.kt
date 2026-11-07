package br.pucpr.authserver.students

import br.pucpr.authserver.common.Sorting
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.security.CurrentUser
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudentService(private val repository: StudentRepository) {

    @Transactional
    fun insert(name: String, email: String): Student {
        val cleanEmail = normalize(email)
        if (repository.findByEmail(cleanEmail) != null) {
            throw ConflictException("Já existe um estudante com o e-mail informado.")
        }
        return repository.save(Student(name = name.trim(), email = cleanEmail))
            .also { log.info("Estudante {} cadastrado", it.id) }
    }

    @Transactional(readOnly = true)
    fun search(name: String?, email: String?, sortBy: String?, sortDir: String?): List<Student> {
        val sort = Sorting.resolve(sortBy, sortDir, SORTABLE_FIELDS, default = "name")
        log.debug("Consultando estudantes (name={}, email={}, sort={})", name, email, sort)
        return repository.findAll(StudentSpecifications.filter(name, email), sort)
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Student =
        repository.findByIdOrNull(id)
            ?: throw NotFoundException("Estudante $id não encontrado.")

    @Transactional
    fun update(id: Long, name: String, email: String): Student {
        val student = findById(id)
        val cleanEmail = normalize(email)
        val sameEmail = repository.findByEmail(cleanEmail)
        if (sameEmail != null && sameEmail.id != id) {
            throw ConflictException("Já existe um estudante com o e-mail informado.")
        }
        student.name = name.trim()
        student.email = cleanEmail
        return repository.save(student)
            .also { log.info("Estudante {} atualizado por {}", id, CurrentUser.label()) }
    }

    @Transactional
    fun delete(id: Long) {
        val student = findById(id)
        val enrollments = student.enrollments.size
        repository.delete(student)
        log.warn("Estudante {} removido por {} (matrículas removidas junto: {})", id, CurrentUser.label(), enrollments)
    }

    private fun normalize(email: String) = email.trim().lowercase()

    companion object {
        private val log = LoggerFactory.getLogger(StudentService::class.java)
        private val SORTABLE_FIELDS = mapOf("id" to "id", "name" to "name", "email" to "email")
    }
}
