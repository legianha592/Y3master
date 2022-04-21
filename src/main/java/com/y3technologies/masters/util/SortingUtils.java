package com.y3technologies.masters.util;

import com.y3technologies.masters.constants.AppConstants;
import com.y3technologies.masters.dto.BaseDto;
import com.y3technologies.masters.dto.table.BaseTableDto;
import com.y3technologies.masters.model.BaseEntity;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class SortingUtils {
    private SortingUtils() {
    }

    public static Comparator<String> getStringComparatorCaseInsensitive(boolean isAscending) {
        Comparator<String> comparator = Comparator.nullsFirst((o1, o2) -> {
            Collator coll = Collator.getInstance(Locale.US);
            coll.setStrength(Collator.IDENTICAL);
            return coll.compare(o1.toUpperCase(), o2.toUpperCase());
        });
        if (isAscending) {
            return comparator;
        }
        return comparator.reversed();
    }

    public static void addDefaultSortExtra (DataTablesInput input) {
        input.addColumn(AppConstants.CommonPropertyName.ID, false, true, "");
        input.addOrder(AppConstants.CommonPropertyName.ID, false);
    }

    public static <T extends BaseDto> Comparator<T> addDefaultSortExtraBaseDto(Comparator<T> comparator) {
        return comparator.thenComparing(T::getId, Comparator.reverseOrder());
    }

    public static <T extends BaseEntity> Comparator<T> addDefaultSortExtraBaseEntity(Comparator<T> comparator) {
        return comparator.thenComparing(T::getId, Comparator.reverseOrder());
    }

    public static <T extends BaseTableDto> Comparator<T> addDefaultSortExtraBaseTableDto(Comparator<T> comparator) {
        return comparator.thenComparing(T::getId, Comparator.reverseOrder());
    }
}
