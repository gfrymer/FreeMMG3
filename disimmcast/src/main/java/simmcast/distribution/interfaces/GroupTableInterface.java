package simmcast.distribution.interfaces;

public interface GroupTableInterface {
	public static final String GP_FNCTN_PREFIX = "GP_";
	public GroupInterface removeGroup(int i_);
	public GroupInterface getGroupById(int i_);
	public int[] getMembersById(int i_);
}
