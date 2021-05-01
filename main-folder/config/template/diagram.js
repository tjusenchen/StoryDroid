var nodes = {};

// Compute the distinct nodes from the links.
links.forEach(function (link) {
    link.source = nodes[link.source] || (nodes[link.source] = {
        name: link.source,
        actcode: link.source_actcode,
        layoutcode: link.source_layoutcode,
        img: link.sourceimg,
        methodLinks: link.methodSourceLink,
        act_fullname: link.source_fullname
    });
    link.target = nodes[link.target] || (nodes[link.target] = {
        name: link.target,
        actcode: link.target_actcode,
        layoutcode: link.target_layoutcode,
        img: link.targetimg,
        methodLinks: link.methodTargetLink,
        act_fullname: link.target_fullname
    });
});

var wholeCode = document.getElementById("one_pre");
var methodCode = document.getElementById("two_pre");

var width = 1100,
    height = 700;

var force = d3.layout.force()
    .nodes(d3.values(nodes))
    .links(links)
    .size([width, height])
    .linkDistance(200)
    .on("tick", tick)
    .gravity(0.05)
    .charge(-3000)
    .linkStrength(1)
    .start();

var svg = d3.select("#vis").append("svg")
    .attr("width", width)
    .attr("height", height);

// Per-type markers, as they don't inherit styles.
svg.append("defs").selectAll("marker")
    .data(["suit", "licensing", "resolved"])
    .enter().append("marker")
    .attr("id", function (d) {
	d.fixed = true;
        return d;
    })
    .attr("viewBox", "0 -5 10 10")
    .attr("refX", 15)
    .attr("refY", -1.5)
    .attr("markerWidth", 6)
    .attr("markerHeight", 6)
    .attr("orient", "auto")
    .append("path")
    .attr("d", "M0,-5L10,0L0,5");

var path = svg.append("g").selectAll("path")
                    .data(force.links())
                    .enter().append("path")
                    .attr("class", function (d) {
                        return "link " + d.type;
                    })
                    .attr("marker-end", function (d) {
                        return "url(#" + d.type + ")";
                    });    
                    
var image = svg.append("g").selectAll("image")
    .data(force.nodes())
    .enter().append("image")
    .attr("xlink:href", function (d) {        
	return d.img;
    })
    .attr("id", (d) => d.name)
    .attr("height", 100)
    .attr("width", 80)
    .attr("x", -40)
    .attr("y", -50)
    .on("mouseover", (d) => {
        d3.select("#" + d.name).attr("class", "")
	.attr("height", 200)
    	.attr("width", 160);
    })
    .on("mouseleave", (d) => {
        d3.select("#" + d.name).attr("class", "")
	.attr("height", 100)
    	.attr("width", 80);
    })
    .on("click", (d) => {
        document.getElementById("img").src = d.img; 
        document.getElementById("appname").innerText = "Activity Name: " + d.act_fullname
        document.getElementById("h1").href = "index2.html?name=" + d.name + "#sourceactcode"
        document.getElementById("h2").href = "index2.html?name=" + d.name + "#sourcelayoutcode"
        document.getElementById("h3").href = "index2.html?name=" + d.name
        document.getElementById("rightpanel").style.visibility = 'visible'

    })
    .call(force.drag);

    
    var text = svg.append("g").selectAll("text")
        .data(force.nodes())
        .enter().append("text")
        .attr("x", -30)
        .attr("y", -60)
        // .attr("y", ".11em")
	   .style("font-size", "13px")
	   .style("font-weight", "bold")
        .text(function (d) {
            return d.name;
    });

// Use elliptical arc path segments to doubly-encode directionality.
function tick() {
    path.attr("d", linkArc);
    text.attr("transform", transform);
    image.attr("transform", transform);
    image.transition();
}

function linkArc(d) {
    var dx = d.target.x - d.source.x,
                dy = d.target.y - d.source.y,
                dr = Math.sqrt(dx * dx + dy * dy);
    var delta_x = 0, delta_y = 0;
    if (Math.abs(dy/dx) > 1.25){
        delta_y = 50 * Math.abs(dy)/ dy;
        delta_x = dx * 50.0 / dr;
    }else{
        delta_x = 40 * Math.abs(dx)/dx;
        delta_y = dy * 40.0 / dr; 
    }
    
    // console.log(dx, dy, dr, delta_x, delta_y, delta_x * delta_x + delta_y * delta_y)
            //return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
            return "M" + (d.source.x + delta_x) + "," + (d.source.y + delta_y) + "L" + (d.target.x - delta_x) + "," + (d.target.y - delta_y);
            //return "M" + (d.source.x) + "," + (d.source.y) + "L" + (d.target.x) + "," + (d.target.y);
}

function transform(d) {
    d.x = Math.max(40, Math.min(width - 40, d.x));
    d.y = Math.max(50, Math.min(height - 50, d.y));
    return "translate(" + d.x + "," + d.y + ")";
}



