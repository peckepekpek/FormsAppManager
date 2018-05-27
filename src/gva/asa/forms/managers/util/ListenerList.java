package gva.asa.forms.managers.util;

import java.util.*;

public class ListenerList extends Vector
{
	public boolean add(Object obj)
	{
		if (0 <= indexOf(obj))
			return false;
		return super.add(obj);
	}
}

