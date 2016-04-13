package com.supermap.desktop.utilties;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetVector;
import com.supermap.data.Datasource;
import com.supermap.data.DatasourceConnectionInfo;
import com.supermap.data.EngineInfo;
import com.supermap.data.EngineType;
import com.supermap.data.ErrorInfo;
import com.supermap.data.Toolkit;
import com.supermap.data.Workspace;
import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.PluginInfo;
import com.supermap.desktop._XMLTag;
import com.supermap.desktop.implement.SmMenu;
import com.supermap.desktop.implement.SmMenuItem;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.ui.XMLCommand;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

/**
 * 数据源工具类
 * 
 * @author highsad
 *
 */
public class DatasourceUtilties {

	// #region Variable
	// private static DataTable CurrentDataTable;

	private static ArrayList<EngineInfo> loadedFileEngines = null;
	private static PluginInfo pluginInfo;

	public static PluginInfo getPluginInfo() {
		return DatasourceUtilties.pluginInfo;
	}

	public static void setPluginInfo(PluginInfo pluginInfo) {
		DatasourceUtilties.pluginInfo = pluginInfo;
	}

	private static SmMenu recentDatasourceMenu = null;

	public static SmMenu getRecentDatasourceMenu() {
		return recentDatasourceMenu;
	}

	public static void setRecentDatasourceMenu(SmMenu recentDatasourceMenu) {
		DatasourceUtilties.recentDatasourceMenu = recentDatasourceMenu;
	}

	/**
	 * 刚刚打开失败的数据源，是不是因为密码错误
	 */
	private static boolean passwordWrong = false;

	public static boolean isPasswordWrong() {
		return passwordWrong;
	}

	// private static EngineType[] fileEngineTypes;
	// private static EngineType[] databaseEngineTypes;
	// private static EngineType[] webEngineTypes;
	// #endregion

	/**
	 * 根据指定名称获取指定数据源下的数据集
	 * 
	 * @param datasetName
	 * @param datasource
	 * @return
	 */
	private DatasourceUtilties() {
		// 工具类不提供构造函数

	}

	public static final Dataset getDataset(String datasetName, Datasource datasource) {
		Dataset result = null;

		try {
			if (null == datasetName || null == datasource) {
				return null;
			}

			result = datasource.getDatasets().get(datasetName);

			// 如果到这里数据集还是 null，那么就扫描网络数据集等的子数据集进行匹配
			if (result == null) {
				for (int i = 0; i < datasource.getDatasets().getCount(); i++) {
					Dataset dataset = datasource.getDatasets().get(i);

					if (dataset instanceof DatasetVector) {
						DatasetVector datasetVector = (DatasetVector) dataset;
						if (datasetVector.getChildDataset() != null && datasetName.equals(datasetVector.getChildDataset().getName())) {
							result = datasetVector.getChildDataset();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}
		return result;
	}

	/**
	 * 关闭所有内存数据源
	 */
	public static void closeMemoryDatasource() {
		for (int i = 0; i < Application.getActiveApplication().getWorkspace().getDatasources().getCount();) {
			Datasource item = Application.getActiveApplication().getWorkspace().getDatasources().get(i);
			if (":memory:".equalsIgnoreCase(item.getConnectionInfo().getServer())) {
				Application.getActiveApplication().getWorkspace().getDatasources().close(item.getAlias());
				// 关闭内存数据源时不保存工作空间
				// Application.getActiveApplication().getWorkspace().Save();
			} else {
				i++;
			}
		}
	}

	/**
	 * 判断工作空间中是否存在内存数据源
	 *
	 * @param workspace
	 * @return
	 */
	public static boolean isContainMemoryDatasource(Workspace workspace) {
		boolean isContian = false;
		try {
			for (int i = 0; i < workspace.getDatasources().getCount(); i++) {
				Datasource datasource = workspace.getDatasources().get(i);
				if (":memory:".equalsIgnoreCase(datasource.getConnectionInfo().getServer())) {
					isContian = true;
					break;
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		return isContian;
	}

	/**
	 * 获取工作空间中的内存数据源
	 *
	 * @param workspace
	 * @return
	 */
	public static String[] getMemoryDatasources(Workspace workspace) {
		ArrayList<String> datasources = new ArrayList<String>();
		try {
			for (int i = 0; i < workspace.getDatasources().getCount(); i++) {
				Datasource datasource = workspace.getDatasources().get(i);
				if (":memory:".equalsIgnoreCase(datasource.getConnectionInfo().getServer())) {
					datasources.add(datasource.getAlias());
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
		return datasources.toArray(new String[datasources.size()]);
	}

	/**
	 * 获取当前组件支持加载打开的文件引擎信息集合
	 *
	 * @return 文件引擎信息集合
	 */
	public static EngineInfo[] getLoadedFileEngines() {
		EngineInfo[] result = null;
		try {
			if (loadedFileEngines == null) {
				loadedFileEngines = new ArrayList<EngineInfo>();
				// modify by huchenpu 2015-07-09
				// 组件有问题，下面这行代码直接抛异常
				// EngineInfo[] infos =
				// Environment.getCurrentLoadedEngineInfos();
				// int length = infos.length;
				// for (EngineInfo engInfo :
				// Environment.getCurrentLoadedEngineInfos()) {
				// if (engInfo.getEngineFamily() == EngineFamilyType.FILE) {
				// loadedFileEngines.add(engInfo);
				// }
				// }

				result = loadedFileEngines.toArray(new EngineInfo[loadedFileEngines.size()]);
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return result;
	}

	/**
	 * 将指定数据源添加到最近文件列表中
	 *
	 * @param datasource
	 */
	public static void addDatasourceToRecentFile(Datasource datasource) {
		try {
			if (datasource != null) {
				String filePath = datasource.getConnectionInfo().getServer().replace("\\", "/");
				// File file = new File(filePath);

				if (recentDatasourceMenu != null) {
					removeRecentFile(filePath);

					XMLCommand xmlCommand = new XMLCommand(pluginInfo);
					xmlCommand.setCtrlActionClass("CtrlActionRecentFiles");
					xmlCommand.setLabel(filePath);
					xmlCommand.setTooltip(filePath);
					SmMenuItem menuItem = new SmMenuItem(null, xmlCommand, recentDatasourceMenu);
					if (menuItem != null) {
						recentDatasourceMenu.insert((IBaseItem) menuItem, 0);
						if (recentDatasourceMenu.getItemCount() > 7) {
							removeRecentFile(recentDatasourceMenu.getItem(7).getText());
						}
						saveRecentFile(filePath);
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public static void initRecentFileMenu() {
		try {
			String recentFilePath = PathUtilties.getFullPathName(_XMLTag.RECENT_FILE_XML, false);
			File file = new File(recentFilePath);
			if (file.exists()) {
				Element element = XmlUtilties.getRootNode(recentFilePath);
				if (element != null) {
					NodeList nodes = element.getChildNodes();
					for (int i = 0; i < nodes.getLength(); i++) {
						if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element item = (Element) (nodes.item(i));
							if (item.getNodeName().equalsIgnoreCase(_XMLTag.g_NodeGroup)) {
								String type = item.getAttribute(_XMLTag.g_ControlLabel);
								if (type.equalsIgnoreCase(CoreProperties.getString("String_RecentDatasource"))) {
									NodeList childnNodes = item.getChildNodes();
									for (int j = 0; j < childnNodes.getLength(); j++) {
										if (childnNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
											Element childItem = (Element) (childnNodes.item(j));
											String filePath = childItem.getAttribute(_XMLTag.g_NodeContent);
											XMLCommand xmlCommand = new XMLCommand(pluginInfo);
											xmlCommand.setCtrlActionClass("CtrlActionRecentFiles");
											xmlCommand.setLabel(filePath);
											xmlCommand.setTooltip(filePath);
											SmMenuItem menuItem = new SmMenuItem(null, xmlCommand, recentDatasourceMenu);
											if (menuItem != null) {
												recentDatasourceMenu.add((IBaseItem) menuItem);
											}
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public static void saveRecentFile(String filePath) {
		try {
			String recentFilePath = PathUtilties.getFullPathName(_XMLTag.RECENT_FILE_XML, false);
			File file = new File(recentFilePath);
			if (file.exists()) {
				Document document = XmlUtilties.getDocument(recentFilePath);
				Element element = document.getDocumentElement();
				if (element != null) {
					NodeList nodes = element.getChildNodes();
					for (int i = 0; i < nodes.getLength(); i++) {
						if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element item = (Element) (nodes.item(i));
							if (item.getNodeName().equalsIgnoreCase(_XMLTag.g_NodeGroup)) {
								String type = item.getAttribute(_XMLTag.g_ControlLabel);
								if (type.equalsIgnoreCase(CoreProperties.getString("String_RecentDatasource"))) {
									// 把原来的记录全部取出来保存
									NodeList childnNodes = item.getChildNodes();
									ArrayList<String> recentFiles = new ArrayList<String>();
									for (int j = childnNodes.getLength() - 1; j >= 0; j--) {
										if (childnNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
											Element childItem = (Element) (childnNodes.item(j));
											recentFiles.add(0, childItem.getAttribute(_XMLTag.g_NodeContent));
										}
										item.removeChild(childnNodes.item(j));
									}

									// 添加新纪录
									Element newItem = document.createElement("item");
									newItem.setAttribute(_XMLTag.g_NodeContent, filePath);
									item.appendChild(newItem);

									// 把之前的记录再写入
									for (int j = 0; j < recentFiles.size(); j++) {
										newItem = document.createElement("item");
										newItem.setAttribute(_XMLTag.g_NodeContent, recentFiles.get(j));
										item.appendChild(newItem);
									}

									// 保存文件
									XmlUtilties.saveXml(recentFilePath, document, document.getXmlEncoding());
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public static void removeRecentFile(String filePath) {
		try {
			for (IBaseItem item : recentDatasourceMenu.items()) {
				if (((SmMenuItem) item).getToolTipText().equals(filePath)) {
					recentDatasourceMenu.remove(item);
				}
			}

			String recentFilePath = PathUtilties.getFullPathName(_XMLTag.RECENT_FILE_XML, false);
			File file = new File(recentFilePath);
			if (file.exists()) {
				Document document = XmlUtilties.getDocument(recentFilePath);
				Element element = document.getDocumentElement();
				if (element != null) {
					NodeList nodes = element.getChildNodes();
					for (int i = 0; i < nodes.getLength(); i++) {
						if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element item = (Element) (nodes.item(i));
							if (item.getNodeName().equalsIgnoreCase(_XMLTag.g_NodeGroup)) {
								String type = item.getAttribute(_XMLTag.g_ControlLabel);
								if (type.equalsIgnoreCase(CoreProperties.getString("String_RecentDatasource"))) {
									NodeList childnNodes = item.getChildNodes();
									for (int j = 0; j < childnNodes.getLength(); j++) {
										if (childnNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
											Element childItem = (Element) (childnNodes.item(j));
											String itemPath = childItem.getAttribute(_XMLTag.g_NodeContent);
											if (itemPath.equalsIgnoreCase(filePath)) {
												item.removeChild(childnNodes.item(j));
											}
										}
									}

									// 保存文件
									XmlUtilties.saveXml(recentFilePath, document, document.getXmlEncoding());
									break;
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public static String getAvailableDatasourceAlias(String alias, int index) {
		int indexTemp = index;
		String availableName = alias;
		try {
			Datasource datasource = null;
			if (indexTemp == 0) {
				datasource = Application.getActiveApplication().getWorkspace().getDatasources().get(availableName);
			} else {
				availableName += "_" + String.valueOf(indexTemp);
				datasource = Application.getActiveApplication().getWorkspace().getDatasources().get(availableName);
			}

			if (datasource != null) {
				indexTemp++;

				availableName = getAvailableDatasourceAlias(alias, indexTemp);
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}

		return availableName;
	}

	/**
	 * 打开指定路径与密码的文件型数据源
	 *
	 * @param fileName
	 * @param passWord
	 * @param isReadOnly
	 * @return
	 */
	public static Datasource openFileDatasource(String fileName, String passWord, boolean isReadOnly) {
		Datasource datasource = null;
		Workspace workspace = Application.getActiveApplication().getWorkspace();
		try {
			((JFrame) Application.getActiveApplication().getMainFrame()).setCursor(Cursor.WAIT_CURSOR);
			DatasourceConnectionInfo info = new DatasourceConnectionInfo();

			// 看看该文件是否已经打开了
			boolean alreadyOpen = false;
			String server = "";
			String fileNameTemp = fileName.toLowerCase();
			fileNameTemp = fileNameTemp.replace("\\", "/");
			for (int index = 0; index < workspace.getDatasources().getCount(); index++) {
				server = workspace.getDatasources().get(index).getConnectionInfo().getServer().toLowerCase();
				server = server.replace("\\", "/");
				if (server.indexOf(".") > -1
						&& (server.equals(fileNameTemp) || (server.substring(0, server.lastIndexOf(".")) + ".udd").equals(fileNameTemp) || (server.substring(0,
								server.lastIndexOf(".")) + ".udb").equals(fileNameTemp))) {
					datasource = workspace.getDatasources().get(index);
					alreadyOpen = true;
					break;
				}
			}

			if (!alreadyOpen) {
				File file = new File(fileName);
				String alias = file.getName();
				String fileExt = "udb";
				int dot = alias.lastIndexOf('.');
				if ((dot > -1) && (dot < (alias.length()))) {
					fileExt = alias.substring(dot, alias.length());
					alias = alias.substring(0, dot);
				}

				// 找一个合适的别名
				alias = getAvailableDatasourceAlias(alias, 0);

				info.setAlias(alias);
				info.setServer(fileName);
				info.setPassword(passWord);
				info.setEngineType(CommonToolkit.EngineTypeWrap.getEngineType(fileExt));
				info.setReadOnly(!file.canWrite() || isReadOnly || info.getEngineType() == EngineType.VECTORFILE);

				try {
					datasource = workspace.getDatasources().open(info);
					if (datasource != null) {
						// 数据源打开成功后将其添加到最近文件列表中 [12/12/2011 zhoujt]
						addDatasourceToRecentFile(datasource);
					}
				} catch (Exception ex) {
					if ("supermap_license_error_wronglicensedata".equalsIgnoreCase(ex.getMessage())) {
						Application.getActiveApplication().getOutput().output(CoreProperties.getString("String_Wronglicensedata"));
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			((JFrame) Application.getActiveApplication().getMainFrame()).setCursor(Cursor.DEFAULT_CURSOR);
		}

		return datasource;
	}

	public static Datasource openFileDatasource(String fileName, String passWord, boolean isReadOnly, boolean isFirst) {
		DatasourceUtilties.passwordWrong = false;
		Datasource datasource = null;
		try {
			((JFrame) Application.getActiveApplication().getMainFrame()).setCursor(Cursor.WAIT_CURSOR);
			Toolkit.clearErrors();
			datasource = openFileDatasource(fileName, passWord, isReadOnly);
			if (datasource == null) {
				ErrorInfo[] errorInfos = Toolkit.getLastErrors(1);

				// 首先判断一下是不是已知的错误，目前已知的就是密码错误
				for (int i = 0; i < errorInfos.length; i++) {
					if (errorInfos[i].getMarker().equals(CoreProperties.getString("String_UGS_PASSWORD"))
							|| errorInfos[i].getMarker().equals(CoreProperties.getString("String_UGS_PASSWORDError"))) {
						DatasourceUtilties.passwordWrong = true;
						break;
					}
				}

				// 不是第一次打开，输出错误信息
				if (!isFirst) {
					if (DatasourceUtilties.passwordWrong) {
						Application.getActiveApplication().getOutput().output(CoreProperties.getString("String_CurrentPasswordWrong"));
					} else {
						for (int i = 0; i < errorInfos.length; i++) {
							Application.getActiveApplication().getOutput().output(errorInfos[i].getMessage());
						}
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			((JFrame) Application.getActiveApplication().getMainFrame()).setCursor(Cursor.DEFAULT_CURSOR);
		}

		return datasource;
	}

}
