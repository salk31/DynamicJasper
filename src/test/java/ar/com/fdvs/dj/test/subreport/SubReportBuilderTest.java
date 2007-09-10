/*
 * Dynamic Jasper: A library for creating reports dynamically by specifying
 * columns, groups, styles, etc. at runtime. It also saves a lot of development
 * time in many cases! (http://sourceforge.net/projects/dynamicjasper)
 *
 * Copyright (C) 2007  FDV Solutions (http://www.fdvsolutions.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 *
 * License as published by the Free Software Foundation; either
 *
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *
 */

package ar.com.fdvs.dj.test.subreport;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;
import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.builders.GroupBuilder;
import ar.com.fdvs.dj.domain.builders.SubReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Stretching;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.ColumnsGroup;
import ar.com.fdvs.dj.domain.entities.Subreport;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import ar.com.fdvs.dj.domain.entities.columns.PropertyColumn;
import ar.com.fdvs.dj.test.ReportExporter;
import ar.com.fdvs.dj.test.TestRepositoryProducts;
import ar.com.fdvs.dj.test.domain.Product;
import ar.com.fdvs.dj.util.SortUtils;

public class SubReportBuilderTest extends TestCase {

	private Map params = new HashMap();

	public DynamicReport buildReport() throws Exception {

		Style detailStyle = new Style();
		Style headerStyle = new Style();
		headerStyle.setFont(Font.ARIAL_MEDIUM_BOLD);
		headerStyle.setBorder(Border.MEDIUM);
		headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
		headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);

		Style titleStyle = new Style();
		titleStyle.setFont(new Font(18, Font._FONT_VERDANA, true));
		Style importeStyle = new Style();
		importeStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
		Style oddRowStyle = new Style();
		oddRowStyle.setBorder(Border.NO_BORDER);
		oddRowStyle.setBackgroundColor(Color.LIGHT_GRAY);
		oddRowStyle.setTransparency(Transparency.OPAQUE);

		DynamicReportBuilder drb = new DynamicReportBuilder();
		Integer margin = new Integer(20);
		drb
			.addTitleStyle(titleStyle)
			.addTitle("November 2006 sales report")					//defines the title of the report
			.addSubtitle("The items in this report correspond "
					+"to the main products: DVDs, Books, Foods and Magazines")				
			.addDetailHeight(new Integer(15)).addLeftMargin(margin)
			.addRightMargin(margin).addTopMargin(margin).addBottomMargin(margin)
			.addPrintBackgroundOnOddRows(true)
			.addOddRowBackgroundStyle(oddRowStyle);

		AbstractColumn columnState = ColumnBuilder.getInstance()
				.addColumnProperty("state", String.class.getName()).addTitle(
						"State").addWidth(new Integer(85))
				.addStyle(detailStyle).addHeaderStyle(headerStyle).build();

		AbstractColumn columnBranch = ColumnBuilder.getInstance()
				.addColumnProperty("branch", String.class.getName()).addTitle(
						"Branch").addWidth(new Integer(85)).addStyle(
						detailStyle).addHeaderStyle(headerStyle).build();

		AbstractColumn columnaProductLine = ColumnBuilder.getInstance()
				.addColumnProperty("productLine", String.class.getName())
				.addTitle("Product Line").addWidth(new Integer(85)).addStyle(
						detailStyle).addHeaderStyle(headerStyle).build();

		AbstractColumn columnaItem = ColumnBuilder.getInstance()
				.addColumnProperty("item", String.class.getName()).addTitle(
						"Item").addWidth(new Integer(85)).addStyle(detailStyle)
				.addHeaderStyle(headerStyle).build();

		AbstractColumn columnCode = ColumnBuilder.getInstance()
				.addColumnProperty("id", Long.class.getName()).addTitle("ID")
				.addWidth(new Integer(40)).addStyle(importeStyle)
				.addHeaderStyle(headerStyle).build();

		AbstractColumn columnaQuantity = ColumnBuilder.getInstance()
				.addColumnProperty("quantity", Long.class.getName()).addTitle(
						"Quantity").addWidth(new Integer(80)).addStyle(
						importeStyle).addHeaderStyle(headerStyle).build();

		AbstractColumn columnAmount = ColumnBuilder.getInstance()
				.addColumnProperty("amount", Float.class.getName()).addTitle(
						"Amount").addWidth(new Integer(90))
				.addPattern("$ 0.00").addStyle(importeStyle).addHeaderStyle(
						headerStyle).build();
		
		/**
		 * Create the subreport. Note that the "subreport" object is then passed
		 * as parameter to the GroupBuilder
		 */
		Subreport subreport = new SubReportBuilder()
						.addDataSource(	DJConstants.SUBREPORT_DATA_SOURCE_ORIGIN_PARAMETER,
										DJConstants.DATA_SOURCE_TYPE_COLLECTION,
										"statistics")
						.addReport(createFooterSubreport())
						.build();

		/**
		 * add in a map the paramter with the data source to use in the subreport.
		 * The "params" map is later passed to the DynamicJasperHelper.generateJasperPrint(...)
		 */
		params.put("statistics", Product.statistics_  ); // the 2nd param is a static Collection	

		/**
		 * Create the group and add the subreport (as a Fotter subreport)
		 */
		GroupBuilder gb1 = new GroupBuilder();
		ColumnsGroup g1 = gb1.addCriteriaColumn((PropertyColumn) columnState)
						.addFooterSubreport(subreport)
						.build();

		Style defaultFooterVariableStyle = new Style();
		defaultFooterVariableStyle.setStreching(Stretching.NO_STRETCH);
		defaultFooterVariableStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
		defaultFooterVariableStyle.setFont(Font.ARIAL_MEDIUM_BOLD);

		drb.addColumn(columnState);
		drb.addColumn(columnBranch);
		drb.addColumn(columnaProductLine);
		drb.addColumn(columnaItem);
		drb.addColumn(columnCode);
		drb.addColumn(columnaQuantity);
		drb.addColumn(columnAmount);
		
		drb.addGroup(g1); // add group g1
		
		drb.addUseFullPageWidth(true);
		
		DynamicReport dr = drb.build();
		
		return dr;
	}

	public void testReport() throws Exception {
		DynamicReport dr = buildReport();
		Collection dummyCollection = TestRepositoryProducts.getDummyCollection();
		dummyCollection = SortUtils.sortCollection(dummyCollection, dr.getColumns());
		
		JRDataSource ds = new JRBeanCollectionDataSource(dummyCollection);
		JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds, params);
		ReportExporter.exportReport(jp, System.getProperty("user.dir") + "/target/SubReportBuilderTest.pdf");
		//JasperViewer.viewReport(jp);
	}
	
	/**
	 * Created and compiles dynamically a report to be used as subreportr
	 * @return
	 * @throws Exception
	 */
	private JasperReport createFooterSubreport() throws Exception {
		FastReportBuilder rb = new FastReportBuilder();
		DynamicReport dr = rb
		.addColumn("Area", "name", String.class.getName(), 100)
		.addColumn("Average", "average", Float.class.getName(), 50)
		.addColumn("%", "percentage", Float.class.getName(), 50)
		.addColumn("Amount", "amount", Float.class.getName(), 50)
		.addGroups(1)
		.addMargins(5, 5, 20, 20)
		.addUseFullPageWidth(true)
		.addTitle("Subreport for this group")
		.build();
		return DynamicJasperHelper.generateJasperReport(dr, new ClassicLayoutManager());
	}


	public static void main(String[] args) throws Exception {
		SubReportBuilderTest test = new SubReportBuilderTest();
		test.testReport();
	}

}