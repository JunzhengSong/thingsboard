/**
 * Copyright © 2016-2023 The Thingsboard Authors
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
package org.thingsboard.server.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.CharSequenceReader;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * /@NoArgsConstructor(access = AccessLevel.PRIVATE): Lombok 注解，生成一个私有的无参构造方法。这样的构造方法通常用于防止在类外部被直接实例化，强制使用静态方法或其他工厂方法创建对象
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CsvUtils {

    public static List<List<String>> parseCsv(String content, Character delimiter) throws Exception {//content内容，delimiter分隔符
        CSVFormat csvFormat = delimiter.equals(',') ? CSVFormat.DEFAULT : CSVFormat.DEFAULT.withDelimiter(delimiter);

        List<CSVRecord> records;
        try (CharSequenceReader reader = new CharSequenceReader(content)) {
            records = csvFormat.parse(reader).getRecords();
        }

        return records.stream()
                .map(record -> Stream.iterate(0, i -> i < record.size(), i -> i + 1)
                        .map(record::get)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        // 1.将records列表转换成stream
        // 2..map对流中的每个元素进行映射
        // 3. 映射方法是：根据这个对象的长度创建一个int类型的stream，然后遍历这个int stream的时候，使用record的get方法，把stream当前的int作为索引，去获取这个对象，作用也就是获取每个字段的值
        // 4.使用.collect方法把一个record对象收集的元素变成一个列表，也就是一行
        // 5.把这每行作为一个元素再把这些行当成一个列表



        //records.stream(): 将 records 列表转换为一个流（Stream）。
        //.map(record -> ...): 对流中的每个元素（每个 CSVRecord 对象）执行映射操作。
        //Stream.iterate(0, i -> i < record.size(), i -> i + 1): 使用 Stream.iterate 创建一个包含整数序列的流，起始值为 0，逐步递增，直到 i < record.size() 不再满足条件。
        //.map(record::get): 对上一步生成的整数流中的每个元素 i，将其作为索引传递给 record::get 方法，从而获取 CSVRecord 中的每个字段的值。这里使用了方法引用 record::get。
        //.collect(Collectors.toList()): 将上一步得到的流中的元素收集为一个列表。
        //.collect(Collectors.toList()): 最外层的 collect 操作，将每一行的列表收集为最终的结果列表
        //整个操作的效果是将 CSV 中的每一行记录（每个 CSVRecord）转换为一个列表，其中包含该行中的所有字段值。最终，整个 CSV 的记录都被映射并收集为一个包含多个列表的列表。
    }

}
