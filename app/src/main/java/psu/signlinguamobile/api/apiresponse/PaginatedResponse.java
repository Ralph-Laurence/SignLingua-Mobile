package psu.signlinguamobile.api.apiresponse;

import java.util.List;

import psu.signlinguamobile.models.Link;

public class PaginatedResponse<T>
{
    public int current_page;
    public List<T> data;
    public String first_page_url;
    public int from;
    public int to;
    public int total;
    public int last_page;
    public String last_page_url;
    public String next_page_url;
    public String prev_page_url;

    public List<Link> links;
    public String searchTerm;
    public String filterValue;
    // and so on...
}

//	"current_page": 1,
//            "data": [{}],
//            "first_page_url": "http://localhost:8000/api/learner-management/learners?page=1",
//            "from": 1,
//            "last_page": 5,
//            "last_page_url": "http://localhost:8000/api/learner-management/learners?page=5",
//	"next_page_url": "http://localhost:8000/api/learner-management/learners?page=2",
//            "path": "http://localhost:8000/api/learner-management/learners",
//            "per_page": 10,
//            "prev_page_url": null,
//            "to": 10,
//            "total": 41