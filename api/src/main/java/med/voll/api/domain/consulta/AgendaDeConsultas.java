package med.voll.api.domain.consulta;

import med.voll.api.domain.ValidacaoException;
import med.voll.api.domain.consulta.validacoes.ValidadorAgendamentoConsulta;
import med.voll.api.domain.medico.Medico;
import med.voll.api.domain.medico.MedicoRepository;
import med.voll.api.domain.paciente.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendaDeConsultas {
    @Autowired
    ConsultaRepository consultaRepository;
    @Autowired
    MedicoRepository medicoRepository;
    @Autowired
    PacienteRepository pacienteRepository;

    @Autowired
    private List<ValidadorAgendamentoConsulta> validadores;

    public DadosDetalhamentoConsulta agendar(DadosAgendamentoConsulta dados){

        if(!pacienteRepository.existsById(dados.idPaciente())) {
            throw new ValidacaoException("Id do paciente informado não existe");
        }

        if(dados.idMedico() != null && !medicoRepository.existsById(dados.idMedico())){
            throw new ValidacaoException("Id do medico informado não existe");
        }

        validadores.forEach(v -> v.validar(dados));

        var paciente = pacienteRepository.getReferenceById(dados.idPaciente());
        var medico = escolherMedicos(dados);

        if (medico == null){
            throw new ValidacaoException("Não existe medico disponivel nesta data");
        }

        var consulta = new Consulta(null, medico, paciente, dados.data(), null);
        consultaRepository.save(consulta);

        return new DadosDetalhamentoConsulta(consulta);

    }

    private Medico escolherMedicos(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null){
            return medicoRepository.getReferenceById(dados.idMedico());
        }

        if (dados.especialidade() == null){
            throw new ValidacaoException("Quando medico não é escolhido inserir a especialidade é obrigatorio!");
        }

        return medicoRepository.escolherMedicoAleatorioLivre(dados.especialidade(), dados.data());
    }

    public void cancelar(DadosCancelamentoConsulta dados) {
        if (!consultaRepository.existsById(dados.idConsulta())) {
            throw new ValidacaoException("Id da consulta informado não existe!");
        }

        var consulta = consultaRepository.getReferenceById(dados.idConsulta());
        consulta.cancelar(dados.motivo());
    }
}
