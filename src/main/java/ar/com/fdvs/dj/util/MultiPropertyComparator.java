package ar.com.fdvs.dj.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Alejandro Gomez
*         Date: Feb 26, 2007
*         Time: 10:27:32 AM
*/
public class MultiPropertyComparator implements Comparator {

    private static final Log LOGGER = LogFactory.getLog(MultiPropertyComparator.class);

    private List info;

    public MultiPropertyComparator(final List _info) {
        info = _info;
    }

    public int compare(final Object _o1, final Object _o2) {
        int result = 0;
        for (int i = 0; result == 0 && i < info.size(); i++) {
            final SortInfo sortInfo = (SortInfo)info.get(i);
            try {
                final String propertyName = sortInfo.getPropertyName();
                final Comparable value1 = getValue(_o1, propertyName);
                final Comparable value2 = getValue(_o2, propertyName);
                result = compare(value1, value2) * (sortInfo.isAscending() ? 1 : -1);
            } catch (IllegalAccessException ex) {
                LOGGER.warn("", ex);
            } catch (InvocationTargetException ex) {
                LOGGER.warn("", ex);
            } catch (NoSuchMethodException ex) {
                LOGGER.warn("", ex);
            }
        }
        return result;
    }

    private static Comparable getValue(final Object _object, final String _field) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Object value = PropertyUtils.getProperty(_object, _field);
        return value instanceof Comparable ? (Comparable)value : value == null ? "" : value.toString();
    }

    @SuppressWarnings("unchecked")
	private static int compare(final Comparable _value1, final Comparable _value2) {
        if (_value1 == null) {
            if (_value2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (_value2 == null) {
            return 1;
        } else {
            return _value1.compareTo(_value2);
        }
    }
}