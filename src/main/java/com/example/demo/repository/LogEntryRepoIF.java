package com.example.demo.repository;

import com.example.demo.model.LogEntry;
import org.springframework.data.repository.CrudRepository;

public interface LogEntryRepoIF  extends CrudRepository<LogEntry, String> {
}
