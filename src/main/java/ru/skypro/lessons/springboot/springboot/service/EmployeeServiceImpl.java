package ru.skypro.lessons.springboot.springboot.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resources;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.lessons.springboot.springboot.dto.EmployeeDTO;

import ru.skypro.lessons.springboot.springboot.exceptions.IdNotFoundException;
import ru.skypro.lessons.springboot.springboot.pojo.Employee;

import ru.skypro.lessons.springboot.springboot.projections.EmployeeView;
import ru.skypro.lessons.springboot.springboot.repository.EmployeeRepository;

import java.awt.print.Pageable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        logger.info("Was invoked method for getting all employees");
        List<Employee> result = new ArrayList<>();
        employeeRepository.findAll().forEach(result::add);
        logger.debug("Database was accessed successfully");

        return result.stream().map(EmployeeDTO::fromEmployee).toList();
    }


    @Override
    public void addEmployee(EmployeeDTO employeeDTO) {
        logger.info("Was invoked method for adding employee");
        Employee employee = employeeDTO.toEmployee();

        employeeRepository.save(employee);
        logger.debug("Database was accessed successfully");
    }


    @Override
    public void refactorEmployeeById(EmployeeDTO employeeDTO, Integer id) {
        logger.info("Was invoked method for refactoring employee");
        Employee employee = employeeDTO.toEmployee();
        employee.setId(id);
        employeeRepository.save(employee);
        logger.debug("Database was accessed successfully");
    }




    @SneakyThrows
    @Override
    public EmployeeDTO getEmployeeById(Integer id) {
        logger.info("Was invoked method for getting employee by id");
        String strId = ""+id;
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IdNotFoundException("Сотрудника по id  "+strId+" не найдено"));
        logger.debug("Database was accessed successfully");

        return EmployeeDTO.fromEmployee(employee);
    }

    @Override
    public void deleteEmployeeById(Integer id) {
        logger.info("Was invoked method for deleting employee by id");
        employeeRepository.deleteById(id);
        logger.debug("Database was accessed successfully");
    }

    @Override
    public List<EmployeeDTO> moreThanDefinedSalary(Integer definedSalary) {
        logger.info("Was invoked method for getting employee with salary more than defined");
        List<EmployeeDTO> employeeDTOList = employeeRepository.findBySalaryGreaterThan(definedSalary)
                .stream()
                .map(EmployeeDTO::fromEmployee).toList();
        logger.debug("Database was accessed successfully");
        return employeeDTOList;
    }

    @Override
    public List<EmployeeView> getEmployeeWithHighestSalary(){
        logger.info("Was invoked method for getting employee with highest salary");
        List<EmployeeView> employeeViewList = new ArrayList<>();

        int max = employeeRepository.findAllEmployeeView().stream()
                .max(Comparator.comparingInt(EmployeeView::getSalary)).get().getSalary();
        logger.debug("Database was accessed successfully");
        employeeRepository.findAllEmployeeView().stream()
                .filter(x->x.getSalary()==max).forEach(employeeViewList::add);
        logger.debug("Database was accessed successfully");
        return  employeeViewList;
    }
    @Override
    public List<EmployeeView> getEmployeesOnPosition(String positionInfo){
        logger.info("Was invoked method for getting employee with highest salary");
        if(positionInfo==null||positionInfo.isBlank()){
            List<EmployeeView> employeeViewList = employeeRepository.findAllEmployeeView();
            logger.debug("Database was accessed successfully");
            return employeeViewList;

        }
        try{

            Integer a  = Integer.parseInt(positionInfo);
            List<EmployeeView> employeeViewList = employeeRepository.findByPositionId(a);
            logger.debug("Database was accessed successfully");
            return employeeViewList;

        }catch (NumberFormatException e){
            List<EmployeeView> employeeViewList = employeeRepository.findByPositionName(positionInfo);
            logger.debug("Database was accessed successfully");
            return employeeViewList;
        }
    }

    @Override
    public List<EmployeeDTO> getEmployeeWithPaging(Integer pageIndex, int unitPerPage) {
        logger.info("Was invoked method for getting employees with paging");
        if(unitPerPage>10)unitPerPage=10;
        if(pageIndex==null)pageIndex=0;

        PageRequest employeeOfConcretePage = PageRequest.of(pageIndex, unitPerPage);
        Page<Employee> page = employeeRepository.findAll(employeeOfConcretePage);
        logger.debug("Database was accessed successfully");
        return page.stream().map(EmployeeDTO::fromEmployee).toList();
    }


    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile file){
        logger.info("Was invoked method for getting employees with paging");
        if(file!=null){
            ObjectMapper objectMapper = new ObjectMapper();

            List<EmployeeDTO> employeeDTOList =
                    objectMapper.readValue(file.getInputStream(), new TypeReference<List<EmployeeDTO>>(){});

            employeeRepository.saveAll(
                    employeeDTOList.stream().map(EmployeeDTO::toEmployee).toList());
            logger.debug("Database was accessed successfully");

        }else{
            System.out.println("Файл не найден");
        }

    }
}
