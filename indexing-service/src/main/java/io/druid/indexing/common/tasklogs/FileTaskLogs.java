/*
 * Druid - a distributed column store.
 * Copyright 2012 - 2015 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.druid.indexing.common.tasklogs;

import com.google.common.base.Optional;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.metamx.common.logger.Logger;
import io.druid.indexing.common.config.FileTaskLogsConfig;
import io.druid.tasklogs.TaskLogs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileTaskLogs implements TaskLogs
{
  private static final Logger log = new Logger(FileTaskLogs.class);

  private final FileTaskLogsConfig config;

  @Inject
  public FileTaskLogs(
      FileTaskLogsConfig config
  )
  {
    this.config = config;
  }

  @Override
  public void pushTaskLog(final String taskid, File file) throws IOException
  {
    if (config.getDirectory().exists() || config.getDirectory().mkdirs()) {
      final File outputFile = fileForTask(taskid);
      Files.copy(file, outputFile);
      log.info("Wrote task log to: %s", outputFile);
    } else {
      throw new IOException(String.format("Unable to create task log dir[%s]", config.getDirectory()));
    }
  }

  @Override
  public Optional<ByteSource> streamTaskLog(final String taskid, final long offset) throws IOException
  {
    final File file = fileForTask(taskid);
    if (file.exists()) {
      return Optional.<ByteSource>of(
          new ByteSource()
          {
            @Override
            public InputStream openStream() throws IOException
            {
              return LogUtils.streamFile(file, offset);
            }
          }
      );
    } else {
      return Optional.absent();
    }
  }

  private File fileForTask(final String taskid)
  {
    return new File(config.getDirectory(), String.format("%s.log", taskid));
  }
}
