package jrtr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * This class represents a geometric object that is stored in a mesh structure.
 * Each vertix knows its adjacent vertices, edges and faces
 * 
 * @author CGG\indermühle
 * 
 */
public class MeshData {

	private VertexData vertexData;
	private List<Vertex> vertexTable;
	protected List<Edge> edgeTable;
	private List<Face> faceTable;
	private RenderContext renderContext;

	public MeshData(VertexData data, RenderContext renderContext) {
		this.renderContext = renderContext;
		this.createMesh(data);
	}

	/**
	 * Helper-Function for the Loop subdivision. Should be always called after a
	 * subdivision so that the Winged Edge Structure and the VertexData are
	 * updated!
	 * 
	 * @param vertexList
	 *            List of all vertices, does not need to have edges, the edges
	 *            are assigned later to vertices
	 * @param i
	 *            Index-List, defines how the vertices are combined into a
	 *            triangle mesh
	 */
	private void createMesh(List<Vertex> vertexList, int[] i) {
		// used by loop subdivision
		for (Vertex v : vertexList)
			v.edge = null;
		vertexTable = vertexList;
		edgeTable = new ArrayList<Edge>(i.length / 2);
		faceTable = new ArrayList<Face>();
		this.createMeshStructure(i, i.length / 3);
	}

	/**
	 * Constructs a winged edge structure from a vertex data. The mesh has to
	 * fulfill two requirements: 1. The mesh must completely consist of
	 * triangles 2. The mesh must have no borders, e.g. each edge must belong to
	 * exactly two triangles
	 * 
	 * @param data
	 *            the VertexData
	 */
	public void createMesh(VertexData data) {

		int nedges = data.getIndices().length / 2;
		int nfaces = data.getIndices().length / 3;

		// initialized the tables for vertices, edges and faces, creates a
		// Vertex object for each vertex and adds it to vertexTable
		vertexTable = new ArrayList<Vertex>(data.getNumberOfVertices());
		edgeTable = new ArrayList<Edge>(nedges);
		faceTable = new ArrayList<Face>(nfaces);

		LinkedList<VertexData.VertexElement> vertexElements = data.getElements();
		ListIterator<VertexData.VertexElement> itr = vertexElements.listIterator(0);

		float[] v = null, c = null, n = null, t = null;
		while (itr.hasNext()) {
			VertexData.VertexElement e = itr.next();
			if (e.getSemantic() == VertexData.Semantic.POSITION)
				v = e.getData();
			if (e.getSemantic() == VertexData.Semantic.COLOR)
				c = e.getData();
			if (e.getSemantic() == VertexData.Semantic.NORMAL)
				n = e.getData();
			if (e.getSemantic() == VertexData.Semantic.TEXCOORD)
				t = e.getData();
		}

		for (int k = 0; k < data.getNumberOfVertices(); k++) {
			Vertex vert = new Vertex(new Vector3f(v[3 * k], v[3 * k + 1], v[3 * k + 2]));
			if (c != null)
				vert.color = new Vector3f(c[3 * k], c[3 * k + 1], c[3 * k + 2]);
			if (n != null)
				vert.normal = new Vector3f(n[3 * k], n[3 * k + 1], n[3 * k + 2]);
			if (t != null)
				vert.texCoord = new Vector2f(t[2 * k], t[2 * k + 1]);
			vertexTable.add(vert);
		}

		this.createMeshStructure(data.getIndices(), nfaces);
	}

	private void createMeshStructure(int[] i, int p) {
		int offset = 0;
		// loop for each face
		for (int k = 0; k < p; k++) {
			// list containing all edges of the face. If an edge doesn't already
			// exist in edgeTable, a new one is created. The edges describe the
			// face in counterclockwise order
			List<Edge> list = new ArrayList<MeshData.Edge>();
			for (int j = 0; j < 3; j++) {
				Edge e = findEdge(i[offset + j], i[offset + (j + 1) % 3]);
				if (e == null) {
					e = new Edge(i[offset + j], i[offset + (j + 1) % 3]);
				}
				list.add(e);
			}
			offset += 3;

			Face f = new Face(list.get(0));
			for (int j = 0; j < list.size(); j++) {
				// adds the face to each of the edges
				if (j == 0)
					list.get(j).addFace(f, list.get(list.size() - 1), list.get(j + 1));
				else
					list.get(j).addFace(f, list.get(j - 1), list.get((j + 1) % list.size()));
				// if one of the vertices doesn't have an edge, it will be
				// defined here
				if (vertexTable.get(list.get(j).v1).edge == null)
					vertexTable.get(list.get(j).v1).edge = list.get(j);
			}
			for (Edge e : list) {
				if (!edgeTable.contains(e))
					edgeTable.add(e);
			}
			faceTable.add(f);
		}
		this.createVertexData();
	}

	/**
	 * Converts the mesh structure into a vertexData and stores it as
	 * this.vertexData
	 */
	private void createVertexData() {
		float[] pos = new float[3 * vertexTable.size()];
		float[] col = new float[3 * vertexTable.size()];
		float[] nrm = new float[3 * vertexTable.size()];
		float[] tex = new float[2 * vertexTable.size()];
		int p = 0;
		int q = 0;
		for (Vertex v : vertexTable) {
			pos[p] = v.position.x;
			col[p] = v.color.x;
			nrm[p] = v.normal.x;
			tex[q] = v.texCoord.x;
			p++;
			q++;
			pos[p] = v.position.y;
			col[p] = v.color.y;
			nrm[p] = v.normal.y;
			tex[q] = v.texCoord.y;
			p++;
			q++;
			pos[p] = v.position.z;
			col[p] = v.color.z;
			nrm[p] = v.normal.z;
			p++;
		}

		// TODO: hope this fixes the problem?
		VertexData data = renderContext.makeVertexData(vertexTable.size());
		data.addElement(pos, VertexData.Semantic.POSITION, 3);
		data.addElement(col, VertexData.Semantic.COLOR, 3);
		data.addElement(nrm, VertexData.Semantic.NORMAL, 3);
		data.addElement(tex, VertexData.Semantic.TEXCOORD, 2);

		List<Integer> index = new ArrayList<Integer>();
		p = 0;
		for (Face f : faceTable) {
			List<Integer> verts = this.findVertices(f);
			index.addAll(verts);
			p++;
		}
		int i[] = new int[index.size()];
		for (int k = 0; k < index.size(); k++) {
			i[k] = index.get(k);
		}
		data.addIndices(i);
		this.vertexData = data;

		// currently the normals are set to be face normals
		vertexData.addElement(getFaceNormals(), VertexData.Semantic.NORMAL, 3);
	}

	/**
	 * Creates a list of faceNormals that can be used in a vertexData. For each
	 * vertex in a face, the x, y and z coordinates of the face normal is added
	 * to the array
	 * 
	 * @return array containing face normals
	 */
	public float[] getFaceNormals() {
		List<Float> faceNormals = new ArrayList<Float>();
		for (Face f : faceTable) {
			Vector3f v = new Vector3f();
			List<Integer> verts = findVertices(f);
			for (int i = 0; i < verts.size(); i++) {
				Vector3f v1 = new Vector3f(vertexTable.get(verts.get(i)).position);
				v1.sub(vertexTable.get(verts.get((i + verts.size() - 1) % verts.size())).position);
				Vector3f v2 = new Vector3f(vertexTable.get(verts.get(i)).position);
				v2.sub(vertexTable.get(verts.get((i + 1) % verts.size())).position);
				v1.cross(v2, v1);
				v.add(v1);
			}
			v.scale(1f / verts.size());
			v.normalize();
			for (int i = 0; i < verts.size(); i++) {
				faceNormals.add(v.x);
				faceNormals.add(v.y);
				faceNormals.add(v.z);
			}
		}
		float[] faceNormalArray = new float[faceNormals.size()];
		for (int i = 0; i < faceNormals.size(); i++) {
			faceNormalArray[i] = faceNormals.get(i);
		}
		return faceNormalArray;
	}

	public VertexData getVertexData() {
		return this.vertexData;
	}

	/**
	 * Finds the edge that conntects the given vertices. If no such edge exists,
	 * null is returned
	 * 
	 * @param i1
	 *            the index in vertexTable of the first vertex
	 * @param i2
	 *            the index in vertexTable of the second vertex
	 * @return the edge that conntects the two vertices or null
	 */
	private Edge findEdge(int i1, int i2) {
		for (Edge e : edgeTable) {
			if (e.connects(i1, i2))
				return e;
		}
		return null;
	}

	/**
	 * Finds all edges connecting the given vertex with other vertices
	 * 
	 * @param v
	 *            the vertex
	 * @return all edges the vertex belongs to
	 */
	private List<Edge> findEdges(Vertex v) {
		List<Edge> edges = new ArrayList<MeshData.Edge>();
		Edge e = v.edge;
		edges.add(e);
		for (Edge g : e.getEdges(v))
			if (!edges.contains(g))
				edges.add(g);
		int k = 1;
		while (k < edges.size()) {
			List<Edge> list = edges.get(k).getEdges(v);
			for (Edge g : list)
				if (!edges.contains(g))
					edges.add(g);
			k++;
		}
		return edges;
	}

	/**
	 * Finds all vertices the given vertex is connected to by an edge
	 * 
	 * @param v
	 *            the vertex
	 * @return all directly connected vertices
	 */
	@SuppressWarnings("unused")
	private List<Vertex> findVertices(Vertex v) {
		List<Edge> edges = findEdges(v);
		List<Vertex> list = new ArrayList<Vertex>(edges.size());
		int i = vertexTable.indexOf(v);
		for (Edge e : edges) {
			if (e.v1 == i)
				list.add(vertexTable.get(e.v2));
			else
				list.add(vertexTable.get(e.v1));
		}
		return list;
	}

	/**
	 * Finds all edges that are part of the face and returns them in
	 * counterclockwise order, starting with the edge defined in f itself
	 * 
	 * @param f
	 *            the face
	 * @return all edges of the face in counterclockwise order
	 */
	private List<Edge> findEdges(Face f) {
		List<Edge> list = new ArrayList<Edge>();
		Edge e = f.edge;
		do {
			list.add(e);
			e = e.getNextEdge(f);
		} while (!f.edge.equals(e));
		return list;
	}

	/**
	 * Finds all vertices of a face and returns them in counterclockwise order,
	 * starting with the starting vertex of the edge defined in f itself. This
	 * does not depend on the orientation of the edge, as it will be oriented to
	 * be counterclockwise
	 * 
	 * @param f
	 *            the face
	 * @return the vertices in counterclockwise order
	 */
	private List<Integer> findVertices(Face f) {
		List<Integer> vertices = new ArrayList<Integer>();
		List<Edge> edges = this.findEdges(f);

		if (edges.get(0).isFrontface(f)) {
			vertices.add(edges.get(0).v1);
			vertices.add(edges.get(0).v2);
		} else {
			vertices.add(edges.get(0).v2);
			vertices.add(edges.get(0).v1);
		}
		edges.remove(0);
		while (edges.size() > 1) {
			int v = vertices.get(vertices.size() - 1);
			for (int k = 0; k < edges.size(); k++) {
				if (edges.get(k).v1 == v) {
					vertices.add(edges.get(k).v2);
					edges.remove(k);
					k = 0;
				} else if (edges.get(k).v2 == v) {
					vertices.add(edges.get(k).v1);
					edges.remove(k);
					k = 0;
				}
			}
		}
		return vertices;
	}

	/**
	 * Subdivide with the Loop-algorithm. This results in a smoother shape that
	 * consists of triangles
	 */
	public void loop() {
		// TODO: this is the part you should implement :-)

		// create new Vertices
		for (int i = 0; i < edgeTable.size(); i++) {
			divideNewVertices(edgeTable.get(i));
		}

		for (int i = 0; i < vertexTable.size(); i++) {
			smoothOldVertices(vertexTable.get(i));
		}

		createNewVertexData();
	}

	private void divideNewVertices(Edge e) {
		Vertex vertex;

		List<Vertex> vertices = getRelevantVertices(e);

		vertex = divideNewVertex(e, vertices);

		// TODO: implement color and normals

		vertexTable.add(vertex);

	}

	private List<Vertex> getRelevantVertices(Edge e) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		vertices.add(new Vertex(vertexTable.get(e.v1)));
		vertices.add(new Vertex(vertexTable.get(e.v2)));
		vertices.add(new Vertex(vertexTable.get(findOppositeVertices(e, 0))));
		vertices.add(new Vertex(vertexTable.get(findOppositeVertices(e, 1))));

		return vertices;
	}

	private Vertex divideNewVertex(Edge e, List<Vertex> vertices) {
		Vertex vertex;
		Vector3f tmp, position, color;

		// ----- Position -----

		tmp = vertices.get(0).position;
		tmp.scale(3.0f / 8.0f);

		position = tmp;

		tmp = vertices.get(1).position;
		tmp.scale(3.0f / 8.0f);

		position.add(tmp);

		tmp = vertices.get(2).position;
		tmp.scale(1.0f / 8.0f);

		position.add(tmp);

		tmp = vertices.get(3).position;
		tmp.scale(1.0f / 8.0f);

		position.add(tmp);

		// ----- Color -----

		tmp = vertices.get(0).color;
		tmp.scale(3.0f / 8.0f);

		color = tmp;

		tmp = vertices.get(1).color;
		tmp.scale(3.0f / 8.0f);

		color.add(tmp);

		tmp = vertices.get(2).color;
		tmp.scale(1.0f / 8.0f);

		color.add(tmp);

		tmp = vertices.get(3).color;
		tmp.scale(1.0f / 8.0f);

		color.add(tmp);

		vertex = new Vertex(position);
		vertex.newPosition = position;
		vertex.color = color;

		vertex.edge = e;
		e.vMid = vertexTable.size();
		return vertex;
	}

	private int findOppositeVertices(Edge e, int i) {

		if (i == 0)
			return findLastVertex(e, e.f1);
		else
			return findLastVertex(e, e.f1);

	}

	private int findLastVertex(Edge e, Face f) {
		List<Integer> vertices = findVertices(f);

		for (Integer v : vertices) {
			if (v != e.v1 && v != e.v2)
				return v;
		}

		return -1; // never reached

	}

	private void smoothOldVertices(Vertex vertex) {

		List<Vertex> adjecant = findVertices(vertex);

		int n = adjecant.size();
		float B = 3.0f / 16.0f;

		if (n > 3)
			B = 3.0f / (8 * n);

		Vector3f sum = new Vector3f(vertex.position);
		sum.scale(1.0f - n * B);

		Vector3f tmp;
		for (Vertex a : adjecant) {
			tmp = new Vector3f(a.position);
			tmp.scale(B);

			sum.add(tmp);
		}

		vertex.newPosition = sum;
	}

	@SuppressWarnings("unused")
	private ArrayList<Vertex> getVertices(Face f) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		List<Integer> indices = findVertices(f);
		for (int i = 0; i < indices.size(); i++) {
			vertices.add(new Vertex(vertexTable.get(indices.get(i))));
		}

		return vertices;
	}

	private void createNewVertexData() {

		List<Integer> newIndices = new ArrayList<Integer>();

		for (Face f : faceTable) {
			// calculate 4 set of indices per face
			List<Edge> edges = findEdges(f);

			addIndices(edges.get(0), edges.get(1), newIndices);
			addIndices(edges.get(1), edges.get(2), newIndices);
			addIndices(edges.get(2), edges.get(0), newIndices);

			// 4th face in the middle
			for (Edge e : edges)
				newIndices.add(e.vMid);
		}

		float[] positions = toPArray(vertexTable);
		float[] colors = toCArray(vertexTable);
		int[] indices = toIArray(newIndices);

		// DEBUG ONLY
		//float[] colors = new float[positions.length];
		//for (int i = 0; i < colors.length; i++){
		//	colors[i] = 1;
		//}
		// DEBUG ONLY

		VertexData vertexData = renderContext.makeVertexData(positions.length / 3);

		vertexData.addElement(positions, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
		vertexData.addIndices(indices);

		this.vertexData = vertexData;
	}

	private float[] toPArray(List<Vertex> list) {
		float[] floats = new float[list.size() * 3];

		int i = 0;
		for (Vertex v : list) {
			floats[i + 0] = v.newPosition.x;
			floats[i + 1] = v.newPosition.y;
			floats[i + 2] = v.newPosition.z;

			i += 3;
		}

		return floats;
	}

	private float[] toCArray(List<Vertex> list) {
		float[] floats = new float[list.size() * 3];

		int i = 0;
		for (Vertex v : list) {
			floats[i + 0] = v.color.x;
			floats[i + 1] = v.color.y;
			floats[i + 2] = v.color.z;

			i += 3;
		}

		return floats;
	}

	private int[] toIArray(List<Integer> newIndices) {

		int[] indices = new int[newIndices.size()];

		for (int i = 0; i < indices.length; i++)
			indices[i] = newIndices.get(i);

		return indices;
	}

	// calculates 3 indices: the midpoint of e1, e2 and the corner the two share together, and adds them to newIndices
	private void addIndices(Edge edge1, Edge edge2, List<Integer> newIndices) {
		newIndices.add(edge1.vMid);
		newIndices.add(getCommonPoint(edge1, edge2));
		newIndices.add(edge2.vMid);
	}

	private int getCommonPoint(Edge edge1, Edge edge2) {

		if (edge1.v1 == edge2.v1)
			return edge1.v1;
		else if (edge1.v1 == edge2.v2)
			return edge1.v1;
		else
			return edge1.v2;
	}

	// -------------- Classes for Edges, Vertices and Faces --------------
	protected class Edge {
		protected int v1, v2, vMid;
		protected Face f1, f2;
		// e0 shares v1 and f1, e1 shares v2 and f1, e2 shares v1 and f2 and e3
		// shares v2 and f2 with this edge
		protected Edge[] edges;

		/**
		 * Creates a new edge that connects the two given vertices. This defines
		 * the orientation of the edge
		 * 
		 * @param start
		 *            the index in vertexTable of the starting vertex
		 * @param end
		 *            the index in vertexTable of the ending vertex
		 */
		public Edge(int start, int end) {
			this.v1 = start;
			this.v2 = end;
			edges = new Edge[4];
		}

		/**
		 * Adds a new face and the two edges belonging both to the face an this
		 * edge
		 * 
		 * @param f
		 *            face that belongs to this edge
		 * @param e1
		 *            edge that connects the given face with this edge's first
		 *            vertex
		 * @param e2
		 *            edge that connects the given face with this edge's second
		 *            vertex
		 */
		public void addFace(Face f, Edge e1, Edge e2) {
			if (f1 == null) {
				f1 = f;
				edges[0] = e1;
				edges[1] = e2;
			} else {
				f2 = f;
				edges[2] = e1;
				edges[3] = e2;
			}
		}

		public boolean connects(int v1, int v2) {
			return (this.v1 == v1 && this.v2 == v2 || this.v1 == v2 && this.v2 == v1);
		}

		public Edge getNextEdge(Face f) {
			List<Edge> list = getEdges(f);
			int v;
			if (isFrontface(f))
				v = v2;
			else
				v = v1;
			if (list.size() < 2)
				return null;
			else if (list.get(0).getStartVertex(f) == v)
				return list.get(0);
			else if (list.get(1).getStartVertex(f) == v)
				return list.get(1);
			else
				return null;
		}

		public List<Edge> getEdges(Face f) {
			List<Edge> e = new ArrayList<MeshData.Edge>(2);
			if (f.equals(f1)) {
				e.add(edges[0]);
				e.add(edges[1]);
			} else if (f.equals(f2)) {
				e.add(edges[2]);
				e.add(edges[3]);
			}
			return e;
		}

		public List<Edge> getEdges(Vertex v) {
			int i = vertexTable.indexOf(v);
			List<Edge> e = new ArrayList<MeshData.Edge>(2);
			for (Edge g : edges) {
				if (g != null && (g.v1 == i || g.v2 == i))
					e.add(g);
			}
			return e;
		}

		public boolean isFrontface(Face f) {
			return this.f1.equals(f);
		}

		public int getStartVertex(Face f) {
			if (this.isFrontface(f)) {
				return v1;
			} else
				return v2;
		}

		public int getEndVertex(Face f) {
			if (this.isFrontface(f)) {
				return v2;
			} else
				return v1;
		}
	}

	protected class Vertex {
		protected Vector3f position, newPosition;
		protected Vector3f color;
		protected Vector3f normal;
		protected Vector2f texCoord;
		protected Edge edge;

		public Vertex(Vector3f position) {
			this.position = position;
			color = new Vector3f();
			normal = new Vector3f();
			texCoord = new Vector2f();
		}

		public Vertex(Vertex vertex) {
			this.position = new Vector3f(vertex.position);
			this.color = new Vector3f(vertex.color);
			this.normal = new Vector3f(vertex.normal);
			this.texCoord = new Vector2f(vertex.texCoord);
		}
	}

	protected class Face {
		protected Edge edge;

		public Face(Edge edge) {
			this.edge = edge;
		}
	}
}
